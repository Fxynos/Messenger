package com.vl.messenger.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.domain.entity.Profile
import com.vl.messenger.domain.usecase.CreateConversationUseCase
import com.vl.messenger.domain.usecase.GetLoggedUserProfileUseCase
import com.vl.messenger.domain.usecase.LogOutUseCase
import com.vl.messenger.domain.usecase.UpdatePhotoUseCase
import com.vl.messenger.domain.usecase.UpdateProfileHiddenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getLoggedUserProfileUseCase: GetLoggedUserProfileUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val updatePhotoUseCase: UpdatePhotoUseCase,
    private val updateProfileHiddenUseCase: UpdateProfileHiddenUseCase,
    private val createConversationUseCase: CreateConversationUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()

    init { fetchProfile() }

    fun logOut() {
        viewModelScope.launch {
            logOutUseCase(Unit)
            _events.emit(DataDrivenEvent.NavigateToAuthScreen)
        }
    }

    fun updatePhoto(imageUri: Uri) {
        if (uiState.value is UiState.Loading)
            return

        viewModelScope.launch(Dispatchers.IO) {
            updatePhotoUseCase(imageUri.toString())
        }.invokeOnCompletion {
            fetchProfile()
        }
    }

    fun setProfileHidden(isHidden: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateProfileHiddenUseCase(isHidden)
        }.invokeOnCompletion {
            fetchProfile()
        }
    }

    fun createConversation(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = createConversationUseCase(name)
                _events.emit(DataDrivenEvent.NavigateToDialog(id))
            } catch (e: Throwable) {
                _events.emit(DataDrivenEvent.NotifyCreatingConversationFailed)
            }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = getLoggedUserProfileUseCase(Unit).asUiState()
        }
    }

    sealed interface UiState {

        data class Loaded(
            val name: String,
            val imageUrl: String?,
            val isUserHidden: Boolean
        ): UiState

        data object Loading: UiState
    }

    sealed interface DataDrivenEvent {
        data object NavigateToAuthScreen: DataDrivenEvent
        data class NavigateToDialog(val dialogId: String): DataDrivenEvent
        data object NotifyCreatingConversationFailed: DataDrivenEvent
    }
}

private fun Profile.asUiState() = ProfileViewModel.UiState.Loaded(
    name = login,
    imageUrl = imageUrl,
    isUserHidden = isHidden
)