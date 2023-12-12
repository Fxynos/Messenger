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

    private val addUserStatement = connection.prepareStatement(
        "insert into user (login, password) values (?, ?);"
    )
    private val getVerboseUserByIdStatement = connection.prepareStatement(
        "select login, password from user where id = ?;"
    )
    private val getVerboseUserByLoginStatement = connection.prepareStatement(
        "select id, password from user where login = ?;"
    )
    private val getUsersByLoginPatternStatement = connection.prepareStatement(
        "select id, login, image from user where login like ? order by login limit 20;"
    )
    private val getFriendRequestId = connection.prepareStatement("""
        select id from notification inner join friend_request on id = notification_id 
        where sender_id = ? and user_id = ?;
    """.trimIndent())
    private val removeNotificationStatement = connection.prepareStatement(
        "delete from notification where id = ?;"
    )
    private val addFriendStatement = connection.prepareStatement(
        "insert into friend (user_id, friend_id) values (?, ?);"
    )
    private val getFriendsStatement = connection.prepareStatement("""
        select id, login, image from (
            select friend_id as id from friend where user_id = ?
            union select user_id as id from friend where friend_id = ?
        ) as ids inner join user using (id);
    """.trimIndent())
    private val areFriendsStatement = connection.prepareStatement("""
        select count(*) as count from (
            select friend_id as id from friend where user_id = ? and friend_id = ?
            union select user_id as id from friend where friend_id = ? and user_id = ?
        ) as ids;
    """.trimIndent())
    private val deleteFromFriendsStatement = connection.prepareStatement(
        "delete from friend where (user_id = ? and friend_id = ?) or (friend_id = ? and user_id = ?);"
    )

    fun addUser(login: String, password: ByteArray) {
        addUserStatement.run {
            setString(1, login)
            setBytes(2, password)
            execute()
        }
    }

    fun getVerboseUser(id: Int) =
        getVerboseUserByIdStatement.run {
            setInt(1, id)
            executeQuery().takeIf { it.next() }?.run {
                VerboseUser(id, getString("login"), getBytes("password"))
            }
        }

    fun getVerboseUser(login: String) =
        getVerboseUserByLoginStatement.run {
            setString(1, login)
            executeQuery().takeIf { it.next() }?.run {
                VerboseUser(getInt("id"), login, getBytes("password"))
            }
        }

    fun getUsersByLogin(login: String): List<User> = // TODO pagination
        getUsersByLoginPatternStatement.run {
            setString(1, "$login%")
            val list = LinkedList<User>()
            val result = executeQuery()
            while (result.next())
                list += User(
                    result.getInt("id"),
                    result.getString("login"),
                    result.getString("image")
                )
            list
        }

    fun getFriendRequestId(senderId: Int, receiverId: Int) =
        getFriendRequestId.run {
            setInt(1, senderId)
            setInt(2, receiverId)
            executeQuery().takeIf(ResultSet::next)?.getLong("id")
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
            ).use { statement ->
                statement.setInt(1, friendId)
                statement.execute()
            }
            connection.prepareStatement(
                "insert into friend_request (notification_id, sender_id) values (LAST_INSERT_ID(), ?);"
            ).use { statement ->
                statement.setInt(1, userId)
                statement.execute()
            }
            connection.commit()
        }
    }

    fun addFriend(userId: Int, friendId: Int) {
        addFriendStatement.run {
            setInt(1, userId)
            setInt(2, friendId)
            execute()
        }
    }

    fun getFriends(userId: Int): List<User> =
        getFriendsStatement.run {
            repeat(2) { i -> setInt(i + 1, userId) }
            val friends = LinkedList<User>()
            executeQuery().run {
                while (next())
                    friends += User(
                        getInt("id"),
                        getString("login"),
                        getString("image")
                    )
                friends
            }
        }

    fun areFriends(userId: Int, friendId: Int) =
        areFriendsStatement.run {
            repeat(2) { i ->
                setInt(i * 2 + 1, userId)
                setInt((i + 1) * 2, friendId)
            }
            executeQuery()
                .also(ResultSet::next)
                .getInt("count") > 0
        }

    fun deleteFriend(userId: Int, friendId: Int) {
        deleteFromFriendsStatement.run {
            repeat(2) { i ->
                setInt(i * 2 + 1, userId)
                setInt((i + 1) * 2, friendId)
            }
            execute()
        }
    }

    /**
     * Deletes notification with supplied id.
     * It also deletes associated friend requests and conversation requests through DBMS cascade deleting.
     */
    fun removeNotification(notificationId: Long) {
        removeNotificationStatement.run {
            setLong(1, notificationId)
            execute()
        }
    }

    fun sendMessage(senderId: Int, receiverId: Int, content: String) {
        if (content.toByteArray().size > 1000)
            throw IllegalArgumentException("Message must be less than 1000 bytes")
        createTransactionalConnection().use { connection ->
            connection.prepareStatement(
                "insert into message (sender_id, content, time) values (?, ?, now());"
            ).use { statement ->
                statement.setInt(1, senderId)
                statement.setString(2, content)
                statement.execute()
            }
            connection.prepareStatement(
                "" // TODO
            )
        }
    }

    class User(val id: Int, val login: String, val image: String?)
    class VerboseUser(val id: Int, val login: String, val password: ByteArray)
}