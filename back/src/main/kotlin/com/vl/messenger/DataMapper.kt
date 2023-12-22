package com.vl.messenger

import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.LinkedList

@Repository
class DataMapper {
    companion object {
        init {
            Class.forName("com.mysql.cj.jdbc.Driver") // fixes SQLException
        }

        private fun createConnection() = DriverManager.getConnection(System.getenv("MSG_DB_URL")
            ?: throw IllegalArgumentException("Define environment variable MSG_DB_URL"))
        private fun createTransactionalConnection() = createConnection().apply { // TODO connections pool
            autoCommit = false
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED // some DBMS anomalies are still can be there
        }
    }

    private val connection = createConnection()

    fun addUser(login: String, password: ByteArray) {
        connection.prepareStatement(
            "insert into user (login, password) values (?, ?);"
        ).use { statement ->
            statement.setString(1, login)
            statement.setBytes(2, password)
            statement.execute()
        }
    }

    fun getVerboseUser(id: Int) =
        connection.prepareStatement(
            "select login, password, image from user where id = ?;"
        ).use { statement ->
            statement.setInt(1, id)
            statement.executeQuery().takeIf { it.next() }?.run {
                VerboseUser(id, getString("login"), getString("image"), getBytes("password"))
            }
        }

    fun getVerboseUser(login: String) =
        connection.prepareStatement(
            "select id, password, image from user where login = ?;"
        ).use { statement ->
            statement.setString(1, login)
            statement.executeQuery().takeIf { it.next() }?.run {
                VerboseUser(getInt("id"), login, getString("image"), getBytes("password"))
            }
        }

    fun getUsersByLogin(login: String): List<User> =
        connection.prepareStatement(
            "select id, login, image from user where login like ? order by login limit 20;" // TODO pagination
        ).use { statement ->
            statement.setString(1, "$login%")
            val list = LinkedList<User>()
            val result = statement.executeQuery()
            while (result.next())
                list += User(
                    result.getInt("id"),
                    result.getString("login"),
                    result.getString("image")
                )
            list
        }

    fun getFriendRequestId(senderId: Int, receiverId: Int) =
        connection.prepareStatement("""
        select id from notification inner join friend_request on id = notification_id 
        where sender_id = ? and user_id = ?;
    """.trimIndent()).use { statement ->
            statement.setInt(1, senderId)
            statement.setInt(2, receiverId)
            statement.executeQuery().takeIf(ResultSet::next)?.getLong("id")
        }

    fun sendFriendRequest(userId: Int, friendId: Int) {
        createTransactionalConnection().use { connection -> // concurrent transactions require different JDBC connections
            connection.prepareStatement(
                """
                    select id from notification inner join friend_request on id = notification_id 
                    where sender_id = ? and user_id = ?;
                """.trimIndent()
            ).run {
                setInt(1, userId)
                setInt(2, friendId)
                if (executeQuery().next())
                    throw IllegalStateException("Friend request is already sent")
            }
            connection.prepareStatement(
                "insert into notification (user_id, time) values (?, now());"
            ).run {
                setInt(1, friendId)
                execute()
            }
            connection.prepareStatement(
                "insert into friend_request (notification_id, sender_id) values (LAST_INSERT_ID(), ?);"
            ).run {
                setInt(1, userId)
                execute()
            }
            connection.commit()
        }
    }

    /**
     * Symmetric
     */
    fun addFriend(userId: Int, friendId: Int) {
        connection.prepareStatement(
            "insert into friend (user_id, friend_id) values (?, ?);"
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setInt(2, friendId)
            statement.execute()
        }
    }

    fun getFriends(userId: Int): List<User> =
        connection.prepareStatement("""
        select id, login, image from (
            select friend_id as id from friend where user_id = ?
            union select user_id as id from friend where friend_id = ?
        ) as ids inner join user using (id);
    """.trimIndent()).use { statement ->
            repeat(2) { i -> statement.setInt(i + 1, userId) }
            val friends = LinkedList<User>()
            statement.executeQuery().run {
                while (next())
                    friends += User(
                        getInt("id"),
                        getString("login"),
                        getString("image")
                    )
                friends
            }
        }

    /**
     * Symmetric
     */
    fun areFriends(userId: Int, friendId: Int) =
        connection.prepareStatement("""
            select count(*) as count from (
                select friend_id as id from friend where user_id = ? and friend_id = ?
                union select user_id as id from friend where friend_id = ? and user_id = ?
            ) as ids;
        """.trimIndent()).use { statement ->
            repeat(2) { i ->
                statement.setInt(i * 2 + 1, userId)
                statement.setInt((i + 1) * 2, friendId)
            }
            statement.executeQuery()
                .also(ResultSet::next)
                .getInt("count") > 0
        }

    /**
     * Symmetric
     */
    fun deleteFriend(userId: Int, friendId: Int) {
        connection.prepareStatement(
            "delete from friend where (user_id = ? and friend_id = ?) or (friend_id = ? and user_id = ?);"
        ).use { statement ->
            repeat(2) { i ->
                statement.setInt(i * 2 + 1, userId)
                statement.setInt((i + 1) * 2, friendId)
            }
            statement.execute()
        }
    }

    /**
     * Deletes notification with supplied id.
     * It also deletes associated friend requests and conversation requests through DBMS cascade deleting.
     */
    fun removeNotification(notificationId: Long) {
        connection.prepareStatement(
            "delete from notification where id = ?;"
        ).use { statement ->
            statement.setLong(1, notificationId)
            statement.execute()
        }
    }

    fun addMessage(senderId: Int, receiverId: Int, content: String) {
        if (content.toByteArray().size > 1000)
            throw IllegalArgumentException("Message must be less than 1000 bytes")
        createTransactionalConnection().use { connection ->
            connection.prepareStatement(
                "insert into message (sender_id, content, time) values (?, ?, now());"
            ).run {
                setInt(1, senderId)
                setString(2, content)
                execute()
            }
            connection.prepareStatement(
                "insert into private_message (message_id, receiver_id) values (last_insert_id(), ?);"
            ).run {
                setInt(1, receiverId)
                execute()
            }
            connection.commit()
        }
    }

    /**
     * Symmetric
     */
    fun getMessages(userId: Int, companionId: Int) {
        connection.prepareStatement("""
            select id, time, content from message inner join private_message 
            where (sender_id = ? and receiver_id = ?) or (receiver_id = ? and sender_id = ?);
        """.trimIndent()).use { statement ->
            /*repeat(2) { i ->
                setInt(i * 2 + 1, userId)
                setInt((i + 1) * 2, friendId)
            } TODO*/
        }
    }

    open class User(val id: Int, val login: String, val image: String?)
    class VerboseUser(id: Int, login: String, image: String?, val password: ByteArray): User(id, login, image)
}