package com.vl.messenger.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vl.messenger.domain.entity.ConversationMember
import com.vl.messenger.domain.entity.Dialog
import com.vl.messenger.domain.entity.Role
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.usecase.AddConversationMemberUseCase
import com.vl.messenger.domain.usecase.DownloadConversationReportUseCase
import com.vl.messenger.domain.usecase.GetAvailableRolesUseCase
import com.vl.messenger.domain.usecase.GetDialogByIdUseCase
import com.vl.messenger.domain.usecase.GetFriendsUseCase
import com.vl.messenger.domain.usecase.GetLoggedUserProfileUseCase
import com.vl.messenger.domain.usecase.GetOwnConversationRoleUseCase
import com.vl.messenger.domain.usecase.GetPagedConversationMembersUseCase
import com.vl.messenger.domain.usecase.RemoveConversationMemberUseCase
import com.vl.messenger.domain.usecase.SetConversationMemberRole
import com.vl.messenger.domain.usecase.UpdateConversationImageUseCase
import com.vl.messenger.domain.usecase.UpdateConversationNameUseCase
import com.vl.messenger.ui.utils.fetch
import com.vl.messenger.ui.utils.launch
import com.vl.messenger.ui.utils.launchHeavy
import com.vl.messenger.ui.utils.sendTo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EditConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDialogByIdUseCase: GetDialogByIdUseCase,
    private val getPagedConversationMembersUseCase: GetPagedConversationMembersUseCase,
    private val addConversationMemberUseCase: AddConversationMemberUseCase,
    private val removeConversationMemberUseCase: RemoveConversationMemberUseCase,
    private val setConversationMemberRole: SetConversationMemberRole,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val getOwnConversationRoleUseCase: GetOwnConversationRoleUseCase,
    private val getAvailableRolesUseCase: GetAvailableRolesUseCase,
    private val getLoggedUserProfileUseCase: GetLoggedUserProfileUseCase,
    private val downloadConversationReportUseCase: DownloadConversationReportUseCase,
    private val updateConversationNameUseCase: UpdateConversationNameUseCase,
    private val updateConversationImageUseCase: UpdateConversationImageUseCase
): ViewModel() {
    companion object {
        const val ARG_DIALOG_ID = "dialogId"
    }

    @Volatile private var collectMembersJob: Job? = null
    @Volatile private var isDownloadingReport = false

    /* Internal State */
    private val dialogId: String = savedStateHandle[ARG_DIALOG_ID]!!
    private val dialog = MutableStateFlow<Dialog?>(null)
    private val members = MutableStateFlow<PagingData<ConversationMember>>(PagingData.empty())
    private val forceUpdate = MutableStateFlow(System.currentTimeMillis())

    private val ownRole: Role by fetch(Role(
        id = 0,
        name = "",
        canGetReports = false,
        canEditData = false,
        canEditMembers = false,
        canEditRights = false
    )) {
        getOwnConversationRoleUseCase(dialogId)
    }
    private val availableRoles: List<Role> by fetch(emptyList()) {
        getAvailableRolesUseCase(dialogId)
    }
    private val ownProfile: User by fetch(User(-1, "", null)) {
        getLoggedUserProfileUseCase(Unit).asUser()
    }

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()
    val uiState = combine(dialog, members, forceUpdate) { dialog, members, forceUpdate ->
        UiState(
            name = dialog?.title,
            imageUrl = dialog?.image,
            members = members,
            rev = forceUpdate
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        launchHeavy {
            dialog.value = getDialogByIdUseCase(dialogId)
            invalidateMembers()
        }
    }

    fun closeScreen() {
        launch {
            DataDrivenEvent.NavigateBack(dialogId) sendTo _events
        }
    }

    fun showPopupOptions() {
        launch {
            DataDrivenEvent.ShowPopupOptions(
                canInviteMembers = ownRole.canEditMembers,
                canDownloadReports = ownRole.canGetReports,
                canEditName = ownRole.canEditData,
                canEditImage = ownRole.canEditData
            ) sendTo _events
        }
    }

    fun showMemberOptions(member: ConversationMember) {
        launch {
            DataDrivenEvent.ShowMemberOptions(
                member = member,
                // user can't kick himself
                canBeRemoved = ownRole.canEditMembers && member.user.id != ownProfile.id,
                canRoleBeAssigned = ownRole.canEditRights
            ) sendTo _events
        }
    }

    fun selectMemberToInvite() {
        launchHeavy {
            DataDrivenEvent.ShowFriendsToInviteDialog(
                getFriendsUseCase(Unit)
            ) sendTo _events
        }
    }

    fun downloadReport() {
        if (isDownloadingReport)
            return

        isDownloadingReport = true
        launchHeavy {
            DataDrivenEvent.NotifyDownloadingReport sendTo _events
            downloadConversationReportUseCase(dialogId)
            DataDrivenEvent.NotifyReportDownloaded(
                "/Downloads/Messenger/report.pdf" // TODO return path from use case
            ) sendTo _events
            isDownloadingReport = false
        }
    }

    fun inviteMember(user: User) {
        launchHeavy {
            addConversationMemberUseCase(
                AddConversationMemberUseCase.Param(
                    dialogId = dialog.value!!.id,
                    userId = user.id
                ))
            invalidateMembers()
            DataDrivenEvent.NotifyMemberAdded(user) sendTo _events
        }
    }

    fun removeMember(member: ConversationMember) {
        launchHeavy {
            removeConversationMemberUseCase(
                RemoveConversationMemberUseCase.Param(
                dialogId,
                member.user.id
            ))
            invalidateMembers()
            DataDrivenEvent.NotifyMemberRemoved(member) sendTo _events
        }
    }

    fun selectRole(member: ConversationMember) {
        launch {
            DataDrivenEvent.ShowRolesToSet(member, availableRoles) sendTo _events
        }
    }

    fun setMemberRole(member: ConversationMember, role: Role) {
        launchHeavy {
            setConversationMemberRole(
                SetConversationMemberRole.Param(
                    dialogId = dialogId,
                    userId = member.user.id,
                    roleId = role.id
                ))
            invalidateMembers()
            DataDrivenEvent.NotifyMemberRoleSet(member.copy(role = role)) sendTo _events
        }
    }

    fun setConversationName(name: String) {
        launchHeavy {
            updateConversationNameUseCase(UpdateConversationNameUseCase.Param(
                dialogId,
                name
            ))
            invalidateConversation()
        }
    }

    fun setConversationImage(uri: Uri) {
        launchHeavy {
            updateConversationImageUseCase(UpdateConversationImageUseCase.Param(
                dialogId,
                uri.toString())
            )
            invalidateConversation()
        }
    }

    private suspend fun invalidateMembers() {
        collectMembersJob?.cancelAndJoin()
        collectMembersJob = launchHeavy {
            getPagedConversationMembersUseCase(dialogId)
                .cachedIn(viewModelScope)
                .collectLatest(members::emit)
        }
    }

    private suspend fun invalidateConversation() {
        launchHeavy {
            dialog.value = getDialogByIdUseCase(dialogId)
            System.currentTimeMillis() sendTo forceUpdate
        }
    }

    data class UiState(
        val name: String? = null,
        val imageUrl: String? = null,
        val members: PagingData<ConversationMember> = PagingData.empty(),
        private val rev: Long = System.currentTimeMillis()
    )

    sealed interface DataDrivenEvent {
        data class NavigateBack(val dialogId: String): DataDrivenEvent
        data class ShowPopupOptions(
            val canInviteMembers: Boolean,
            val canDownloadReports: Boolean,
            val canEditName: Boolean,
            val canEditImage: Boolean
        ): DataDrivenEvent
        data class ShowFriendsToInviteDialog(val users: List<User>): DataDrivenEvent
        data class NotifyMemberAdded(val member: User): DataDrivenEvent
        data class NotifyMemberRemoved(val member: ConversationMember): DataDrivenEvent
        data class NotifyMemberRoleSet(val member: ConversationMember): DataDrivenEvent
        data class ShowMemberOptions(
            val member: ConversationMember,
            val canBeRemoved: Boolean,
            val canRoleBeAssigned: Boolean
        ): DataDrivenEvent
        data class ShowRolesToSet(val member: ConversationMember, val roles: List<Role>): DataDrivenEvent
        data object NotifyDownloadingReport: DataDrivenEvent
        data class NotifyReportDownloaded(val pathToFile: String): DataDrivenEvent
    }
}