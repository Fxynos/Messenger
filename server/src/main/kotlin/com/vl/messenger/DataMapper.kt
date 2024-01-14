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

        private fun ResultSet.collectUsers(): List<User> {
            val list = LinkedList<User>()
            while (next())
                list += User(
                    getInt("id"),
                    getString("login"),
                    getString("image")
                )
            return list
        }

        private fun ResultSet.collectRoles(): List<ConversationMember.Role> {
            val list = LinkedList<ConversationMember.Role>()
            while (next())
                list += fetchRole()
            return list
        }

        private fun ResultSet.fetchRole() = ConversationMember.Role(
            getInt("conversation_rights.id"),
            getString("role"),
            getBoolean("get_reports"),
            getBoolean("edit_data"),
            getBoolean("edit_members"),
            getBoolean("edit_rights")
        )
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
            "select id, login, password, image from user where login = ?;"
        ).use { statement ->
            statement.setString(1, login)
            statement.executeQuery().takeIf { it.next() }?.run {
                VerboseUser(
                    getInt("id"),
                    getString("login"),
                    getString("image"),
                    getBytes("password")
                )
            }
        }

    fun getUsersByLogin(login: String, fromId: Int?, limit: Int): List<User> =
        connection.prepareStatement(
            """
                select id, login, image from user 
                where ${ if (fromId == null) "" else " id < ? and " }
                login like ? order by id desc limit ?;
            """.trimIndent()
        ).use { statement ->
            var argCounter = 0
            if (fromId != null)
                statement.setInt(++argCounter, fromId)
            statement.setString(++argCounter, "$login%")
            statement.setInt(++argCounter, limit)
            statement.executeQuery().collectUsers()
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
            statement.executeQuery().collectUsers()
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

    fun addMessage(senderId: Int, receiverId: Int, content: String): Long =
        createTransactionalConnection().use { connection ->
            connection.prepareStatement(
                "insert into message (sender_id, content, time) values (?, ?, now());"
            ).run {
                setInt(1, senderId)
                setString(2, content)
                execute()
            }
            val messageId = connection.prepareStatement("select last_insert_id() as id;")
                .executeQuery()
                .also(ResultSet::next)
                .getLong("id")
            connection.prepareStatement(
                "insert into private_message (message_id, receiver_id) values (last_insert_id(), ?);"
            ).run {
                setInt(1, receiverId)
                execute()
            }
            connection.commit()
            messageId
        }

    /**
     * Symmetric
     */
    fun getPrivateMessages(userId: Int, companionId: Int, fromId: Long?, limit: Int): List<Message> =
        connection.prepareStatement("""
            select id, sender_id, time, content from message inner join private_message 
            on id = message_id where ${ if (fromId == null) "" else "id < ? and " } 
            ((sender_id = ? and receiver_id = ?) or (receiver_id = ? and sender_id = ?)) 
            order by id desc limit ?;
        """.trimIndent()).use { statement ->
            var argCounter = 0
            if (fromId != null)
                statement.setLong(++argCounter, fromId)
            statement.setInt(++argCounter, userId)
            statement.setInt(++argCounter, companionId)
            statement.setInt(++argCounter, userId)
            statement.setInt(++argCounter, companionId)
            statement.setInt(++argCounter, limit)
            statement.executeQuery().run {
                val list = LinkedList<Message>()
                while (next())
                    list += Message(
                        getLong("id"),
                        getInt("sender_id"),
                        getTimestamp("time").time / 1000,
                        getString("content")
                    )
                list
            }
        }

    fun getDialogs(userId: Int): List<User> =
        connection.prepareStatement("""
            select id, login, image from (
                with members as (
                    select sender_id, receiver_id from message inner join private_message on id = message_id 
                    where (sender_id = ? or receiver_id = ?)
                ) select sender_id as id from members 
                union select receiver_id as id from members
            ) as ids inner join user using (id) where id != ?;
        """.trimIndent()).use { statement ->
            repeat(3) { i -> statement.setInt(i + 1, userId) }
            statement.executeQuery().collectUsers()
        }

    fun createConversation(userId: Int, name: String): Long =
        createTransactionalConnection().use { connection ->
            connection.prepareStatement("insert into conversation (name) values (?);").run {
                setString(1, name)
                execute()
            }
            connection.prepareStatement("""
                insert into participate (user_id, conversation_id, rights_id) 
                select ?, last_insert_id(), id from conversation_rights where role = "owner";
            """.trimIndent()).run {
                setInt(1, userId)
                execute()
            }
            val conversationId = connection.prepareStatement("select last_insert_id() as id;")
                .executeQuery()
                .also(ResultSet::next)
                .getLong("id")
            connection.commit()
            conversationId
        }

    fun getConversation(id: Long): Conversation? =
        connection.prepareStatement("select name, image from conversation where id = ?;").use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().takeIf(ResultSet::next)?.run {
                Conversation(id, getString("name"), getString("image"))
            }
        }

    fun setConversationName(id: Long, name: String) {
        connection.prepareStatement("update conversation set name = ? where id = ?;").use { statement ->
            statement.setString(1, name)
            statement.setLong(2, id)
            statement.execute()
        }
    }

    fun getMembers(conversationId: Long): List<ConversationMember> = // TODO pagination
        connection.prepareStatement("""
            select * from participate inner join conversation_rights on rights_id = id 
            inner join user on user_id = user.id 
            where conversation_id = ?;
        """.trimIndent()).use { statement ->
            statement.setLong(1, conversationId)
            statement.executeQuery().run {
                val list = LinkedList<ConversationMember>()
                while (next())
                    list += ConversationMember(
                        getInt("user.id"),
                        getString("login"),
                        getString("image"),
                        fetchRole()
                    )
                list
            }
        }

    fun addMember(userId: Int, conversationId: Long) {
        connection.prepareStatement("""
                insert into participate (user_id, conversation_id, rights_id) 
                select ?, ?, id from conversation_rights where role = "member";
            """.trimIndent()).use { statement ->
            statement.setInt(1, userId)
            statement.setLong(2, conversationId)
            statement.execute()
        }
    }

    /**
     * Fail-Safe operation: removes member if it actually exists
     */
    fun removeMember(userId: Int, conversationId: Long) {
        connection.prepareStatement(
            "delete from participate where user_id = ? and conversation_id = ?;"
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setLong(2, conversationId)
            statement.execute()
        }
    }

    val roles: List<ConversationMember.Role>
        get() = connection.createStatement()
            .executeQuery("select * from conversation_rights")
            .collectRoles()
    fun getRole(userId: Int, conversationId: Long): ConversationMember.Role? =
        connection.prepareStatement("""
            select * from participate inner join conversation_rights on rights_id = id 
            where user_id = ? and conversation_id = ?;
        """.trimIndent()).use { statement ->
            statement.setInt(1, userId)
            statement.setLong(2, conversationId)
            statement.executeQuery().takeIf(ResultSet::next)?.fetchRole()
        }

    /**
     * Fail-Safe operation: changes member if it actually exists
     */
    fun setRole(userId: Int, conversationId: Long, role: String) {
        connection.prepareStatement("""
            update participate inner join user on user_id = id 
            inner join conversation on conversation_id = conversation.id 
            set rights_id = (select id from conversation_rights where role = ?) 
            where user_id = ? and conversation_id = ?;
        """.trimIndent()).use { statement ->
            statement.setString(1, role)
            statement.setInt(2, userId)
            statement.setLong(3, conversationId)
            statement.execute()
        }
    }

    open class User(val id: Int, val login: String, val image: String?)

    class VerboseUser(id: Int, login: String, image: String?, val password: ByteArray): User(id, login, image)

    class Message(val id: Long, val senderId: Int, val unixSec: Long, val content: String)

    class Conversation(val id: Long, val name: String, val image: String?)

    class ConversationMember(id: Int, login: String, image: String?, val role: Role): User(id, login, image) {
        class Role(
            val id: Int,
            val name: String,
            val canGetReports: Boolean,
            val canEditData: Boolean,
            val canEditMembers: Boolean,
            val canEditRights: Boolean
        )
    }
}