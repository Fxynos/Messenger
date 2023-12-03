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

    private val addUserStatement = connection.prepareStatement("insert into user (login, password) values (?, ?);")
    private val getUserId = connection.prepareStatement("select id from user where login = ?;")
    private val getPasswordStatement = connection.prepareStatement("select password from user where login = ?;")
    private val getUsersByLoginPatternStatement = connection.prepareStatement("select id, login, image from user where login like ? order by login limit 20;")
    private val addFriendStatement = connection.prepareStatement("insert into friend (user_id, friend_id) values (?, ?);")
    private val getFriendRequestId = connection.prepareStatement("""
            select id from notification inner join friend_request on id = notification_id 
            where sender_id = ? and user_id = ?;
            """.trimIndent())

    fun addUser(login: String, password: ByteArray) {
        addUserStatement.run {
            setString(1, login)
            setBytes(2, password)
            execute()
        }
    }

    fun getUserId(login: String) =
        getUserId.run {
            setString(1, login)
            executeQuery().takeIf(ResultSet::next)?.getInt("id")
        }

    fun getPasswordHash(login: String): ByteArray? =
        getPasswordStatement.run {
            setString(1, login)
            executeQuery().run {
                if (next())
                    this.getBytes("password")
                else
                    null
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
                "select id from notification inner join friend_request on id = notification_id where sender_id = ? and user_id = ?;"
            ).run {
                setInt(1, userId)
                setInt(2, friendId)
                if (executeQuery().next())
                    throw IllegalStateException("Friend request is already sent")
            }
            connection.prepareStatement("insert into notification (user_id, time) values (?, now());").use { statement ->
                statement.setInt(1, friendId)
                statement.execute()
            }
            connection.prepareStatement("insert into friend_request (notification_id, sender_id) values (LAST_INSERT_ID(), ?);").use { statement ->
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

    data class User(val id: Int, val login: String, val image: String?)
}