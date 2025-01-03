package com.vl.messenger.chat

import com.vl.messenger.DataMapper
import com.vl.messenger.PdfService
import com.vl.messenger.StorageService
import com.vl.messenger.asConversationDialogId
import com.vl.messenger.profile.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.Locale

@Service
class ConversationService(
    @Autowired private val dataMapper: DataMapper,
    @Autowired private val storageService: StorageService,
    @Autowired private val pdfService: PdfService,
    @Autowired private val notificationService: NotificationService,
    @Qualifier("messageSource") private val messageSource: MessageSource
) {
    fun createConversation(userId: Int, name: String) =
        dataMapper
            .createConversation(userId, name)
            .asConversationDialogId()

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

    /**
     * Get role of currently authenticated user
     */
    fun getRole(userId: Int, conversationId: Long): CommonResultValue<DataMapper.ConversationMember.Role> =
        dataMapper.getRole(userId, conversationId)
            ?.let { CommonResultValue.Success(it) }
            ?: CommonResultValue.NoPrivilege

    /**
     * Get roles specific for certain conversation
     */
    fun getRoles(userId: Int, conversationId: Long): CommonResultValue<List<DataMapper.ConversationMember.Role>> =
        if (!hasPrivilege(userId, conversationId, Privilege.PARTICIPATE))
            CommonResultValue.NoPrivilege
        else
            CommonResultValue.Success(dataMapper.roles) // roles are shared for all conversations yet

    fun getMembers(userId: Int, conversationId: Long, offset: Int, limit: Int): GetMembersResult {
        if (getConversation(conversationId) == null)
            return GetMembersResult.NotFound

        if (!hasPrivilege(userId, conversationId, Privilege.PARTICIPATE))
            return GetMembersResult.NoPrivilege

        return GetMembersResult.Success(
            dataMapper.getMembers(conversationId, offset, limit)
        )
    }

    fun inviteMember(userId: Int, conversationId: Long, memberId: Int): CommonResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_MEMBERS))
            return CommonResult.NO_PRIVILEGE

        notificationService.sendConversationInviteNotification(userId, memberId, conversationId)
        return CommonResult.SUCCESS
    }

    /**
     * @param inviteId notification id
     */
    fun acceptInvite(userId: Int, inviteId: Long, locale: Locale) {
        val invite = dataMapper.getNotification(userId, inviteId)
                as DataMapper.ConversationRequest

        dataMapper.addMember(userId, invite.conversation.id)
        dataMapper.removeNotification(inviteId)
        notificationService.sendInfoNotification(
            invite.sender.id,
            messageSource.getMessage("notification.new_member.title", null, locale),
            messageSource.getMessage("notification.new_member.content", arrayOf(
                dataMapper.getVerboseUser(userId)!!.login,
                invite.conversation.name
            ), locale)
        )
    }

    fun removeMember(userId: Int, conversationId: Long, memberId: Int): CommonResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_MEMBERS))
            return CommonResult.NO_PRIVILEGE

        dataMapper.removeMember(memberId, conversationId)
        notificationService.sendInfoNotification(
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

    fun setMemberRole(userId: Int, conversationId: Long, memberId: Int, roleId: Int): SetRoleResult {
        if (!hasPrivilege(userId, conversationId, Privilege.EDIT_RIGHTS))
            return SetRoleResult.NO_PRIVILEGE

        if (dataMapper.roles.firstOrNull { it.id == roleId } == null)
            return SetRoleResult.ROLE_NOT_FOUND

        dataMapper.setRole(memberId, conversationId, roleId)
        return SetRoleResult.SUCCESS
    }

    fun isMember(userId: Int, conversationId: Long) =
        dataMapper.getRole(userId, conversationId) != null

    private fun getConversation(conversationId: Long) = dataMapper.getConversation(conversationId)

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

    enum class SetRoleResult {
        SUCCESS,
        NO_PRIVILEGE,
        ROLE_NOT_FOUND
    }

    sealed interface CommonResultValue<out T> {
        data object NoPrivilege: CommonResultValue<Nothing>
        data class Success<T>(val value: T): CommonResultValue<T>
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