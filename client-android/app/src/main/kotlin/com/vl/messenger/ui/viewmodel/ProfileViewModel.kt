package com.vl.messenger.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.usecase.GetLoggedUserProfileUseCase
import com.vl.messenger.domain.usecase.LogOutUseCase
import com.vl.messenger.domain.usecase.UpdatePhotoUseCase
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
    private val updatePhotoUseCase: UpdatePhotoUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = getLoggedUserProfileUseCase(Unit).toUi()
        }
    }

    fun logOut() {
        viewModelScope.launch {
            logOutUseCase(Unit)

        }
    }

    fun updatePhoto(imageUri: Uri) {
        if (uiState.value is UiState.Loading)
            return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            updatePhotoUseCase(imageUri.toString())
            _uiState.value = getLoggedUserProfileUseCase(Unit).toUi()
        }
    }

    sealed interface UiState {

        data class Loaded(
            val name: String,
            val imageUrl: String?
        ): UiState

        data object Loading: UiState
    }

    sealed interface DataDrivenEvent {
        data object NavigateToAuthScreen: DataDrivenEvent
    }
}

private fun User.toUi() = ProfileViewModel.UiState.Loaded(
    name = login,
    imageUrl = imageUrl
)