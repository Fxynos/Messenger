package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.domain.entity.FriendStatus
import com.vl.messenger.domain.entity.VerboseUser
import com.vl.messenger.domain.usecase.AddFriendUseCase
import com.vl.messenger.domain.usecase.GetUserByIdUseCase
import com.vl.messenger.domain.usecase.RemoveFriendUseCase
import com.vl.messenger.ui.utils.launch
import com.vl.messenger.ui.utils.launchHeavy
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

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()

    private val userId = savedStateHandle.get<Int>(ARG_KEY_USER_ID)!!

    init {
        launchHeavy {
            _uiState.value = getUserByIdUseCase(userId).toUi()
        }
    }

    fun addFriend() {
        launchHeavy {
            addFriendUseCase(userId)
            _uiState.value = getUserByIdUseCase(userId).toUi()
        }
    }

    fun removeFriend() {
        launchHeavy {
            removeFriendUseCase(userId)
            _uiState.value = getUserByIdUseCase(userId).toUi()
        }
    }

    fun openDialog() {
        launch {
            _events.emit(DataDrivenEvent.NavigateToDialog("u$userId"))
        }
    }

    data class UiState(
        val name: String? = null,
        val status: FriendStatus? = null,
        val imageUrl: String? = null,
        val availableAction: AvailableAction? = null
    ) {
        enum class AvailableAction {
            ADD_FRIEND,
            REMOVE_FRIEND
        }
    }

    sealed interface DataDrivenEvent {
        data class NavigateToDialog(val dialogId: String): DataDrivenEvent
    }
}

private fun VerboseUser.toUi() = UserProfileViewModel.UiState(
    name = user.login,
    imageUrl = user.imageUrl,
    status = friendStatus,
    availableAction = when (friendStatus) {
        FriendStatus.REQUEST_GOTTEN, FriendStatus.NONE ->
            UserProfileViewModel.UiState.AvailableAction.ADD_FRIEND
        FriendStatus.REQUEST_SENT, FriendStatus.FRIEND ->
            UserProfileViewModel.UiState.AvailableAction.REMOVE_FRIEND
    }
)