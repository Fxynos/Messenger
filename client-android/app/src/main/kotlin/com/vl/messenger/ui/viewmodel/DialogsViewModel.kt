package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.vl.messenger.domain.usecase.GetPagedDialogsUseCase
import com.vl.messenger.ui.UiMapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DialogsViewModel @Inject constructor(
    getPagedDialogsUseCase: GetPagedDialogsUseCase
): ViewModel() {
    val uiState = getPagedDialogsUseCase(Unit)
        .cachedIn(viewModelScope)
        .map { pagingData ->
            pagingData.map { it.toUi() }
        }
}