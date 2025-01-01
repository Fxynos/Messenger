package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.vl.messenger.domain.entity.Notification
import com.vl.messenger.domain.usecase.AcceptConversationInviteUseCase
import com.vl.messenger.domain.usecase.AcceptFriendRequestUseCase
import com.vl.messenger.domain.usecase.GetPagedNotificationsUseCase
import com.vl.messenger.ui.utils.launchHeavy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getPagedNotificationsUseCase: GetPagedNotificationsUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val acceptConversationInviteUseCase: AcceptConversationInviteUseCase
): ViewModel() {
    private var pagingJob: Job? = null

    private val _uiState = MutableStateFlow(PagingData.empty<Notification>())
    val uiState = _uiState.asStateFlow()

    init { invalidate() }

    fun acceptConversationInvite(invite: Notification.InviteToConversation) {
        launchHeavy {
            acceptConversationInviteUseCase(invite.id)
        }.invokeOnCompletion {
            invalidate()
        }
    }

    fun acceptFriendRequest(request: Notification.FriendRequest) {
        launchHeavy {
            acceptFriendRequestUseCase(request.id)
        }.invokeOnCompletion {
            invalidate()
        }
    }

    private fun invalidate() {
        pagingJob?.cancel()
        pagingJob = getPagedNotificationsUseCase(Unit)
            .onEach(_uiState::emit)
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }
}