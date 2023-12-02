package com.vl.messenger.auth.dao

import org.springframework.stereotype.Repository
import java.sql.DriverManager

@Repository
class UserRepository {
    companion object {
        init {
            Class.forName("com.mysql.cj.jdbc.Driver") // fixes SQLException
        }
    }

    private val connection = DriverManager.getConnection(System.getenv("MSG_DB_URL")
        ?: throw IllegalArgumentException("Define environment variable MSG_DB_URL"))

    fun addUser(login: String, password: ByteArray) {
        connection.prepareStatement("INSERT INTO user (login, password) VALUES (?, ?);").use {
            it.setString(1, login)
            it.setBytes(2, password)
            it.execute()
        }
    }

    fun getPasswordHash(login: String): ByteArray? =
        connection.prepareStatement("SELECT password FROM user WHERE login = ?;").use {
            it.setString(1, login)
            it.executeQuery().run {
                if (next())
                    this.getBytes("password")
                else
                    null
            }
        }
}