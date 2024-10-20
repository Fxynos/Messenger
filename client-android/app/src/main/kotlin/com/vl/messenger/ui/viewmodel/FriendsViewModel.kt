package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.usecase.GetFriendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    getFriendsUseCase: GetFriendsUseCase
): ViewModel() {

    val uiState: StateFlow<UiState> = flow {
        emit(UiState.Loaded(
            getFriendsUseCase(Unit)
        ))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    sealed interface UiState {
        data object Loading: UiState
        data class Loaded(val friends: List<User>): UiState
    }
}