package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.vl.messenger.data.component.DialogPagingSource
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.DialogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialogsViewModel @Inject constructor(
    dialogManager: DialogManager
): ViewModel() {
    val dialogs = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { DialogPagingSource(dialogManager) }
    ).flow.cachedIn(viewModelScope)
}