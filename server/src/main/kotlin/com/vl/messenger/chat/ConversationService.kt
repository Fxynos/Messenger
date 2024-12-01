package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.PdfService
import com.vl.messenger.StorageService
import com.vl.messenger.asConversationDialogId
import com.vl.messenger.profile.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.OutputStream

@Service
class ConversationService(
    @Autowired private val dataMapper: DataMapper,
    @Autowired private val storageService: StorageService,
    @Autowired private val pdfService: PdfService,
    @Autowired private val notificationService: NotificationService
) {
    fun createConversation(userId: Int, name: String) =
        dataMapper
            .createConversation(userId, name)
            .asConversationDialogId()

    fun getConversation(conversationId: Long) = dataMapper.getConversation(conversationId)

    fun setConversationName(conversationId: Long, name: String) = dataMapper.setConversationName(conversationId, name)

    fun setConversationImage(conversationId: Long, image: MultipartFile) {
        val path = storageService.saveConversationImage(image, conversationId)
        dataMapper.setConversationImage(conversationId, path)
    }

    fun getMembers(conversationId: Long, offset: Int, limit: Int) = dataMapper.getMembers(conversationId, offset, limit)

    /**
     * @param userId id of competent user having privilege to manage members
     * @param memberId id of user being added
     */
    fun addMember(userId: Int, conversationId: Long, memberId: Int) { // TODO add members via invite rather than directly
        dataMapper.addMember(memberId, conversationId)
        notificationService.addNotification(
            memberId,
            "Новая беседа",
            "Пользователь ${
                dataMapper.getVerboseUser(userId)!!.login
            } добавил вас в беседу ${
                dataMapper.getConversation(conversationId)!!.name
            }"
        )
    }

    fun removeMember(userId: Int, conversationId: Long, memberId: Int) {
        dataMapper.removeMember(memberId, conversationId)
        notificationService.addNotification(
            memberId,
            "Беседы",
            "Пользователь ${
                dataMapper.getVerboseUser(userId)!!.login
            } удалил вас из беседы ${
                dataMapper.getConversation(conversationId)!!.name
            }"
        )
    }

    fun leaveConversation(userId: Int, conversationId: Long) =
        dataMapper.removeMember(userId, conversationId)

    fun setMemberRole(conversationId: Long, memberId: Int, role: String) =
        dataMapper.setRole(memberId, conversationId, role)

    fun isMember(userId: Int, conversationId: Long) =
        dataMapper.getRole(userId, conversationId) != null

    fun hasPrivilege(userId: Int, conversationId: Long, privilege: Privilege): Boolean {
        val role = dataMapper.getRole(userId, conversationId)
            ?: return false
        return when (privilege) {
            Privilege.PARTICIPATE -> true
            Privilege.EDIT_DATA -> role.canEditData
            Privilege.EDIT_MEMBERS -> role.canEditMembers
            Privilege.EDIT_RIGHTS -> role.canEditRights
            Privilege.GET_REPORTS -> role.canGetReports
        }
    }

    enum class Privilege {
        PARTICIPATE, // whether user is conversation member
        EDIT_DATA,
        EDIT_MEMBERS,
        EDIT_RIGHTS,
        GET_REPORTS
    }

    /**
     * Writes pdf file to output stream
     */
    fun generateReport(conversationId: Long, outputStream: OutputStream): Unit =
        pdfService.generateConversationActivityReport(dataMapper.getUsersActivity(conversationId), outputStream)
}