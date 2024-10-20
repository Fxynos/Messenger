package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.domain.entity.FriendStatus
import com.vl.messenger.domain.entity.VerboseUser
import com.vl.messenger.domain.usecase.AddFriendUseCase
import com.vl.messenger.domain.usecase.GetUserByIdUseCase
import com.vl.messenger.domain.usecase.RemoveFriendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val addFriendUseCase: AddFriendUseCase,
    private val removeFriendUseCase: RemoveFriendUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    companion object {
        const val ARG_KEY_USER_ID = "user_id"
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()

    init {
        val userId = savedStateHandle.get<Int>(ARG_KEY_USER_ID)!!
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loaded(getUserByIdUseCase(userId))
        }
    }

    fun addFriend() {
        val profile = (uiState.value as? UiState.Loaded)
            ?.profile
            ?: return

        viewModelScope.launch(Dispatchers.IO) {
            addFriendUseCase(profile.user.id)
            val newFriendStatus = when (profile.friendStatus) {
                FriendStatus.REQUEST_GOTTEN -> FriendStatus.FRIEND
                FriendStatus.NONE -> FriendStatus.REQUEST_SENT
                else -> null
            }
            if (newFriendStatus != null)
                _uiState.value = UiState.Loaded(
                    profile.copy(friendStatus = newFriendStatus)
                )
        }
    }

    fun removeFriend() {
        val profile = (uiState.value as? UiState.Loaded)
            ?.profile
            ?: return

        viewModelScope.launch(Dispatchers.IO) {
            removeFriendUseCase(profile.user.id)
            val newFriendStatus = when (profile.friendStatus) {
                FriendStatus.REQUEST_SENT -> FriendStatus.NONE
                FriendStatus.FRIEND -> FriendStatus.NONE
                else -> null
            }
            if (newFriendStatus != null)
                _uiState.value = UiState.Loaded(
                    profile.copy(friendStatus = newFriendStatus)
                )
        }
    }

    fun openDialog() {
        val user = (uiState.value as? UiState.Loaded)
            ?.profile
            ?.user
            ?: return

        viewModelScope.launch {
            _events.emit(DataDrivenEvent.NavigateToDialog("u${user.id}"))
        }
    }

    sealed interface UiState {
        data object Loading: UiState
        data class Loaded(val profile: VerboseUser): UiState
    }

    sealed interface DataDrivenEvent {
        data class NavigateToDialog(val dialogId: String): DataDrivenEvent
    }
}