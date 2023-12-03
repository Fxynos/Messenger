package com.vl.messenger

import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DriverManager
import java.util.LinkedList
import java.util.logging.Logger

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
    private val getPasswordStatement = connection.prepareStatement("select password from user where login = ?;")
    private val getUsersByLoginPatternStatement = connection.prepareStatement("select id, login, password, image from user where login like ? order by login limit 20;")
    private val addFriendStatement = connection.prepareStatement("insert into friend (user_id, friend_id) values (?, ?);")

    fun addUser(login: String, password: ByteArray) {
        addUserStatement.run {
            setString(1, login)
            setBytes(2, password)
            execute()
        }
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

    fun getUsersByLogin(login: String): List<User> =
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