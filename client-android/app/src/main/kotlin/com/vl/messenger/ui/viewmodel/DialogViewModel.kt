package com.vl.messenger.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vl.messenger.data.component.CacheDao
import com.vl.messenger.data.component.PrivateMessagesPagingSource
import com.vl.messenger.data.component.PrivateMessagesRemoteMediator
import com.vl.messenger.data.component.PrivateMessagesRepository
import com.vl.messenger.data.entity.Conversation
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.entity.PrivateDialog
import com.vl.messenger.data.manager.DialogManager
import com.vl.messenger.data.manager.DownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialogViewModel @Inject constructor(
    private val downloadManager: DownloadManager,
    private val dialogManager: DialogManager
): ViewModel() {
    private val cachedMessages = CacheDao<Long, Message>()
    private val _dialog = MutableStateFlow<Dialog?>(null)

    val uiState = _dialog.map {
        if (it == null)
            UiState.Loading
        else
            UiState.DialogShown(it.name, it.image?.let(downloadManager::downloadBitmap))
    }.flowOn(Dispatchers.IO)

    private lateinit var _messages: Flow<PagingData<Message>>
    val messages by this::_messages

    @OptIn(ExperimentalPagingApi::class)
    fun initialize(dialog: Dialog) {
        if (_dialog.value != null) // skip if initialized
            return

        this._dialog.value = dialog
        _messages = when (dialog) {
            is PrivateDialog -> {
                PrivateMessagesRepository(
                    remoteMediator = PrivateMessagesRemoteMediator(
                        dialogManager,
                        cachedMessages,
                        dialog.id
                    ),
                    pagingSourceFactory = {
                        PrivateMessagesPagingSource(cachedMessages).apply {
                            viewModelScope.launch {
                                cachedMessages.updateEvents.collect { invalidate() }
                            }
                        }
                    }
                ).getMessages().cachedIn(viewModelScope)
            }

            is Conversation -> TODO()
        }
    }

    sealed interface UiState {
        object Loading: UiState
        data class DialogShown(val dialogName: String, val dialogImage: Bitmap?): UiState
    }
}