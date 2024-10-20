package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.usecase.GetPagedUsersByNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getPagedUsersByNameUseCase: GetPagedUsersByNameUseCase
): ViewModel() {
    private var pagingJob: Job? = null
    private val _uiState = MutableStateFlow<PagingData<User>>(PagingData.from(emptyList()))
    val uiState = _uiState.asStateFlow()

    fun search(pattern: String) {
        pagingJob?.cancel()
        pagingJob = getPagedUsersByNameUseCase(pattern)
            .onEach(_uiState::emit)
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        pagingJob?.cancel()
    }
}