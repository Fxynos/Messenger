package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.PdfService
import com.vl.messenger.StorageService
import com.vl.messenger.asConversationDialogId
import com.vl.messenger.profile.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
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

    fun setConversationName(userId: Int, conversationId: Long, name: String): CommonResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_DATA))
            return CommonResult.NO_PRIVILEGE

        dataMapper.setConversationName(conversationId, name)
        return CommonResult.SUCCESS
    }

    fun setConversationImage(userId: Int, conversationId: Long, image: MultipartFile): CommonResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_DATA))
            return CommonResult.NO_PRIVILEGE

        val path = storageService.saveConversationImage(image, conversationId)
        dataMapper.setConversationImage(conversationId, path)
        return CommonResult.SUCCESS
    }

    fun getMembers(userId: Int, conversationId: Long, offset: Int, limit: Int): GetMembersResult {
        if (getConversation(conversationId) == null)
            return GetMembersResult.NotFound

        if (!hasPrivilege(userId, conversationId, Privilege.PARTICIPATE))
            return GetMembersResult.NoPrivilege

        return GetMembersResult.Success(
            dataMapper.getMembers(conversationId, offset, limit)
        )
    }

    fun addMember(userId: Int, conversationId: Long, memberId: Int): CommonResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_MEMBERS))
            return CommonResult.NO_PRIVILEGE

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
        return CommonResult.SUCCESS
    }

    fun removeMember(userId: Int, conversationId: Long, memberId: Int): CommonResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_MEMBERS))
            return CommonResult.NO_PRIVILEGE

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
        return CommonResult.SUCCESS
    }

    fun leaveConversation(userId: Int, conversationId: Long) =
        dataMapper.removeMember(userId, conversationId)

    fun setMemberRole(userId: Int, conversationId: Long, memberId: Int, role: String): CommonResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_RIGHTS))
            return CommonResult.NO_PRIVILEGE

        dataMapper.setRole(memberId, conversationId, role)
        return CommonResult.SUCCESS
    }

    fun isMember(userId: Int, conversationId: Long) =
        dataMapper.getRole(userId, conversationId) != null

    private fun hasPrivilege(userId: Int, conversationId: Long, privilege: Privilege): Boolean {
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
    fun generateReport(userId: Int, conversationId: Long): GenerateReportResult {
        if (!hasPrivilege(userId, conversationId, Privilege.GET_REPORTS))
            return GenerateReportResult.NoPrivilege

        return ByteArrayOutputStream().let {
            pdfService.generateConversationActivityReport(dataMapper.getUsersActivity(conversationId), it)
            GenerateReportResult.Success(it.toByteArray())
        }
    }

    enum class CommonResult {
        SUCCESS,
        NO_PRIVILEGE
    }

    sealed interface GetMembersResult {
        data object NoPrivilege: GetMembersResult
        data object NotFound: GetMembersResult
        data class Success(val members: List<DataMapper.ConversationMember>): GetMembersResult
    }

    sealed interface GenerateReportResult {
        data class Success(val reportFile: ByteArray): GenerateReportResult
        data object NoPrivilege: GenerateReportResult
    }
}