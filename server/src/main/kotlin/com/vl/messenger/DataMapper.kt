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
        private fun createTransactionalConnection() = createConnection().apply {
            autoCommit = false
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED // some DBMS anomalies are still can be there
        }

        private fun ResultSet.getUnixSeconds(label: String): Long = getTimestamp(label).time / 1000

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

        private fun ResultSet.collectMessages(): List<Message> {
            val list = LinkedList<Message>()
            while (next())
                list += Message(
                    getLong("id"),
                    getInt("sender_id"),
                    getUnixSeconds("time"),
                    getString("content")
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

    fun setUserVisibility(userId: Int, isHidden: Boolean) {
        connection.prepareStatement("update user set hidden = ? where id = ?;").use { statement ->
            statement.setBoolean(1, isHidden)
            statement.setInt(2, userId)
            statement.execute()
        }
    }

    /**
     * Fail-Safe
     */
    fun setProfileImage(userId: Int, image: String) {
        connection.prepareStatement("update user set image = ? where id = ?;").use { statement ->
            statement.setString(1, image)
            statement.setInt(2, userId)
            statement.execute()
        }
    }

    fun getUsersByLogin(login: String, fromId: Int?, limit: Int): List<User> =
        connection.prepareStatement(
            """
                select id, login, image from user 
                where hidden = 0 and ${ if (fromId == null) "" else "id < ? and " }
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

    fun addFriendRequest(userId: Int, friendId: Int) {
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

    fun addNotification(userId: Int, title: String, content: String) {
        connection.prepareStatement(
            "insert into notification (user_id, title, content, time) values (?, ?, ?, now());"
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setString(2, title)
            statement.setString(3, content)
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

    fun getNotifications(userId: Int, fromId: Long?, limit: Int): List<Notification> =
        connection.prepareStatement("""
            select
                fr.notification_id,
                friend_sender.id,
                friend_sender.login,
                friend_sender.image,
                notification.id,
                time,
                title,
                content,
                seen,
                cr.notification_id,
                conversation_sender.id,
                conversation_sender.login,
                conversation_sender.image,
                conversation.id,
                conversation.name,
                conversation.image,
                count(participate.user_id) as members
            from notification 
            left join friend_request as fr on id = notification_id 
            left join conversation_request as cr on id = cr.notification_id 
            left join user as friend_sender on fr.sender_id = friend_sender.id 
            left join user as conversation_sender on cr.sender_id = conversation_sender.id 
            left join conversation on conversation_id = conversation.id 
            left join participate on conversation.id = participate.conversation_id
            and user_id = ? ${ if (fromId == null) "" else "and notification.id < ? " }
            order by notification.id desc limit ?;
        """.trimIndent()).use { statement ->
            var argCounter = 0
            statement.setInt(++argCounter, userId)
            if (fromId != null)
                statement.setLong(++argCounter, fromId)
            statement.setInt(++argCounter, limit)
            statement.executeQuery().run { // fr and cr are empty for plain notifications
                val list = LinkedList<Notification>()
                while (next())
                    list += when {
                        getLong("fr.notification_id").let { !wasNull() } -> FriendRequest(
                            User(
                                getInt("friend_sender.id"),
                                getString("friend_sender.login"),
                                getString("friend_sender.image")
                            ),
                            getLong("notification.id"),
                            getUnixSeconds("time"),
                            getString("title"),
                            getString("content"),
                            getBoolean("seen")
                        )
                        getLong("cr.notification_id").let { !wasNull() } -> ConversationRequest(
                            User(
                                getInt("conversation_sender.id"),
                                getString("conversation_sender.login"),
                                getString("conversation_sender.image")
                            ),
                            Conversation(
                                getLong("conversation.id"),
                                getString("conversation.name"),
                                getString("conversation.image"),
                                getInt("members")
                            ),
                            getLong("notification.id"),
                            getUnixSeconds("time"),
                            getString("title"),
                            getString("content"),
                            getBoolean("seen")
                        )
                        else -> PlainNotification(
                            getLong("notification.id"),
                            getUnixSeconds("time"),
                            getString("title"),
                            getString("content"),
                            getBoolean("seen")
                        )
                    }
                list
            }
        }

    /**
     * Returns exactly notifications, not friend and conversation requests
     */
    fun getPlainNotifications(userId: Int, fromId: Long?, limit: Int): List<PlainNotification> =
        connection.prepareStatement("""
            select id, time, title, content, seen from notification 
            left join friend_request as fr on id = notification_id 
            left join conversation_request as cr on id = cr.notification_id 
            where fr.notification_id is null and cr.notification_id is null 
            and user_id = ? ${ if (fromId == null) "" else "and id < ? " }
            order by id desc limit ?;
        """.trimIndent()).use { statement ->
            var argCounter = 0
            statement.setInt(++argCounter, userId)
            if (fromId != null)
                statement.setLong(++argCounter, fromId)
            statement.setInt(++argCounter, limit)
            statement.executeQuery().run {
                val list = LinkedList<PlainNotification>()
                while (next())
                    list += PlainNotification(
                        getLong("notification_id"),
                        getUnixSeconds("time"),
                        getString("title"),
                        getString("content"),
                        getBoolean("seen")
                    )
                list
            }
        }

    /**
     * Fail-Safe
     */
    fun markNotificationAsSeen(notificationId: Long) {
        connection.prepareStatement("update notification set seen = true where id = ?;").use { statement ->
            statement.setLong(1, notificationId)
            statement.execute()
        }
    }

    fun hasNotification(userId: Int, notificationId: Long): Boolean =
        connection.prepareStatement("select count(*) as has from notification where user_id = ? and id = ?;")
            .use { statement ->
                statement.setInt(1, userId)
                statement.setLong(2, notificationId)
                statement.executeQuery().also(ResultSet::next).getBoolean("has")
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
            statement.executeQuery().collectMessages()
        }

    @Deprecated("Use getDialogs() method instead, it is more verbose and provides pagination")
    fun getPrivateDialogs(userId: Int): List<User> =
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
        connection.prepareStatement("""
            select name, image, count(user_id) as members
            from conversation 
            left join participate on id = conversation_id
            where id = ?;
        """.trimIndent()).use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().takeIf(ResultSet::next)?.run {
                Conversation(
                    id,
                    getString("name"),
                    getString("image"),
                    getInt("members")
                )
            }
        }

    /**
     * Get dialogs using offset pagination
     * @return private dialogs and conversations with last message and its sender
     */
    fun getDialogs(userId: Int, offset: Int, limit: Int): List<Dialog> =
        connection.prepareStatement("""
            with private_dialog as (
                select 
                    if (sender_id = ?, receiver_id, sender_id) as dialog_id,    -- id of current user
                    max(message_id) as message_id
                from message
                inner join private_message on id = message_id
                where (sender_id = ? or receiver_id = ?)    -- id of current user twice
                group by dialog_id
            ),
            dialog as (
                -- private dialog --
                select
                    true as is_private,
                    dialog_id,
                    login as dialog_title,
                    image as dialog_image,
                    message_id
                from private_dialog inner join user on dialog_id = user.id
                -- conversation --
                union select
                    false as is_private,
                    id as dialog_id,
                    name as dialog_title,
                    image as dialog_image,
                    if (count(*) > 0, max(message_id), null) as message_id
                from participate inner join conversation on conversation_id = id
                left join conversation_message on id = conversation_message.conversation_id
                where user_id = ?   -- id of current user
                group by conversation.id
            ) select
                is_private,
                dialog_id,
                dialog_title,
                dialog_image,
                message.id as message_id,
                time,
                content,
                user.id,
                login,
                image
            from dialog
            left join message on message_id = message.id
            left join user on sender_id = user.id
            order by message_id desc, dialog_id desc
            limit ? offset ?;   -- offset pagination
        """.trimIndent()).use { statement ->
            var argCounter = 0
            repeat(4) { statement.setInt(++argCounter, userId) }
            statement.setInt(++argCounter, limit)
            statement.setInt(++argCounter, offset)

            statement.executeQuery().run {
                val list = LinkedList<Dialog>()
                while (next())
                    list += Dialog(
                        getBoolean("is_private"),
                        getLong("dialog_id"),
                        getString("dialog_title"),
                        getString("dialog_image"),
                        if (getLong("message_id") == 0L) null else Message(
                            getLong("message_id"),
                            getInt("user.id"),
                            getUnixSeconds("time"),
                            getString("content")
                        ),
                        if (getInt("user.id") == 0) null else User(
                            getInt("user.id"),
                            getString("login"),
                            getString("image")
                        )
                    )
                list
            }
        }

    fun setConversationName(id: Long, name: String) {
        connection.prepareStatement("update conversation set name = ? where id = ?;").use { statement ->
            statement.setString(1, name)
            statement.setLong(2, id)
            statement.execute()
        }
    }

    /**
     * @param image path to image in static resources
     */
    fun setConversationImage(id: Long, image: String) {
        connection.prepareStatement("update conversation set image = ? where id = ?;").use { statement ->
            statement.setString(1, image)
            statement.setLong(2, id)
            statement.execute()
        }
    }

    fun getMembers(conversationId: Long, offset: Int, limit: Int): List<ConversationMember> =
        connection.prepareStatement("""
            select * from participate inner join conversation_rights on rights_id = id 
            inner join user on user_id = user.id 
            where conversation_id = ?
            limit ? offset ?;
        """.trimIndent()).use { statement ->
            var argCounter = 0
            statement.setLong(++argCounter, conversationId)
            statement.setInt(++argCounter, limit)
            statement.setInt(++argCounter, offset)

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

    fun getAllMembers(conversationId: Long): List<ConversationMember> =
        connection.prepareStatement("""
            select * from participate inner join conversation_rights on rights_id = id 
            inner join user on user_id = user.id 
            where conversation_id = ?;
        """.trimIndent()).use { statement ->
            statement.setLong(0, conversationId)

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

    fun getConversationMessages(conversationId: Long, fromId: Long?, limit: Int): List<Message> =
        connection.prepareStatement("""
            select id, sender_id, time, content from message inner join conversation_message on id = message_id 
            where conversation_id = ?${ if (fromId == null) "" else " and id < ?" } 
            order by id desc limit ?;
        """.trimIndent()).use { statement ->
            var argCounter = 0
            statement.setLong(++argCounter, conversationId)
            if (fromId != null)
                statement.setLong(++argCounter, fromId)
            statement.setInt(++argCounter, limit)
            statement.executeQuery().collectMessages()
        }

    fun addConversationMessage(userId: Int, conversationId: Long, content: String): Long =
        createTransactionalConnection().use { connection ->
            connection.prepareStatement(
                "insert into message (sender_id, content, time) values (?, ?, now());"
            ).run {
                setInt(1, userId)
                setString(2, content)
                execute()
            }
            val messageId = connection.prepareStatement("select last_insert_id() as id;")
                .executeQuery()
                .also(ResultSet::next)
                .getLong("id")
            connection.prepareStatement(
                "insert into conversation_message (message_id, conversation_id) values (last_insert_id(), ?);"
            ).run {
                setLong(1, conversationId)
                execute()
            }
            connection.commit()
            messageId
        }

    /**
     * Fail-Fast
     */
    fun addToBlacklist(userId: Int, blockedId: Int) {
        connection.prepareStatement(
            "insert into black_list (user_id, blocked_id) values (?, ?);"
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setInt(2, blockedId)
            statement.execute()
        }
    }

    fun isInBlacklist(userId: Int, blockedId: Int): Boolean =
        connection.prepareStatement(
            "select count(*) as blocked from black_list where user_id = ? and blocked_id = ?;"
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setInt(2, blockedId)
            statement.executeQuery().also(ResultSet::next).getBoolean("blocked")
        }

    fun getBlacklist(userId: Int): List<User> =
        connection.prepareStatement(
            "select * from black_list inner join user on blocked_id = id where user_id = ?;"
        ).use { statement ->
            statement.setInt(1, userId)
            statement.executeQuery().collectUsers()
        }

    /**
     * Fail-Safe
     */
    fun removeFromBlacklist(userId: Int, blockedId: Int) {
        connection.prepareStatement(
            "delete from black_list where user_id = ? and blocked_id = ?;"
        ).use { statement ->
            statement.setInt(1, userId)
            statement.setInt(2, blockedId)
            statement.execute()
        }
    }

    fun getUsersActivity(conversationId: Long): List<UserActivity> =
        connection.prepareStatement("""
            select count(*) as count, sum(length(content)) as chars, user.id, login, image from message 
            inner join conversation_message on id = message_id 
            inner join user on sender_id = user.id 
            where conversation_id = ? 
            group by sender_id order by count desc;
        """.trimIndent()).use { statement ->
            statement.setLong(1, conversationId)
            statement.executeQuery().run {
                val list = LinkedList<UserActivity>()
                while (next())
                    list += UserActivity(
                        User(
                            getInt("id"),
                            getString("login"),
                            getString("image")
                        ),
                        getLong("count"),
                        getLong("chars")
                    )
                list
            }
        }

    open class User(val id: Int, val login: String, val image: String?)

    class VerboseUser(id: Int, login: String, image: String?, val password: ByteArray): User(id, login, image)

    class Message(val id: Long, val senderId: Int, val unixSec: Long, val content: String)

    class Conversation(val id: Long, val name: String, val image: String?, val membersCount: Int)

    /**
     * @param id user id if [isPrivate] `true`, conversation id otherwise
     */
    data class Dialog(
        val isPrivate: Boolean,
        val id: Long,
        val title: String,
        val image: String?,
        val lastMessage: Message?,
        val lastMessageSender: User?
    )

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

    sealed class Notification(
        val id: Long,
        val unixSec: Long,
        val title: String,
        val content: String,
        val isSeen: Boolean
    )

    class PlainNotification(notificationId: Long, unixSec: Long, title: String, content: String, isSeen: Boolean):
        Notification(notificationId, unixSec, title, content, isSeen)

    class FriendRequest(
        val sender: User,
        notificationId: Long, unixSec: Long, title: String, content: String, isSeen: Boolean
    ): Notification(notificationId, unixSec, title, content, isSeen)

    class ConversationRequest(
        val sender: User,
        val conversation: Conversation,
        notificationId: Long, unixSec: Long, title: String, content: String, isSeen: Boolean
    ): Notification(notificationId, unixSec, title, content, isSeen)

    class UserActivity(val user: User, val messages: Long, val characters: Long)
}