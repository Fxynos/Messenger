package com.vl.messenger

import org.springframework.stereotype.Repository
import java.sql.DriverManager
import java.util.LinkedList

@Repository
class DataMapper {
    companion object {
        init {
            Class.forName("com.mysql.cj.jdbc.Driver") // fixes SQLException
        }
    }

    private val connection = DriverManager.getConnection(System.getenv("MSG_DB_URL")
        ?: throw IllegalArgumentException("Define environment variable MSG_DB_URL"))

    private val addUserStatement = connection.prepareStatement("insert into user (login, password) values (?, ?);")
    private val getPasswordStatement = connection.prepareStatement("select password from user where login = ?;")
    private val getUsersByLoginPatternStatement = connection.prepareStatement("select id, login, password, image from user where login like ? order by login limit 20;")

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

    data class User(val id: Int, val login: String, val image: String?)
}