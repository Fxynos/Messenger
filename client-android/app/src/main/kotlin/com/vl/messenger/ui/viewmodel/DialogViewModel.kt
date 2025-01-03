package com.vl.messenger.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.vl.messenger.domain.boundary.PagingCache
import com.vl.messenger.domain.entity.Dialog
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.usecase.GetDialogByIdUseCase
import com.vl.messenger.domain.usecase.GetLoggedUserProfileUseCase
import com.vl.messenger.domain.usecase.GetPagedMessagesUseCase
import com.vl.messenger.domain.usecase.GetUserByIdUseCase
import com.vl.messenger.domain.usecase.LeaveConversationUseCase
import com.vl.messenger.domain.usecase.ObserveAllIncomingMessagesUseCase
import com.vl.messenger.domain.usecase.SendMessageUseCase
import com.vl.messenger.ui.UiMapper.toUi
import com.vl.messenger.ui.adapter.MessagePagingAdapter
import com.vl.messenger.ui.utils.launch
import com.vl.messenger.ui.utils.launchHeavy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val TAG = "DialogViewModel"

@HiltViewModel
class DialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getLoggedUserProfileUseCase: GetLoggedUserProfileUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    getDialogByIdUseCase: GetDialogByIdUseCase,
    getPagedMessagesUseCase: GetPagedMessagesUseCase,
    observeAllIncomingMessagesUseCase: ObserveAllIncomingMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val leaveConversationUseCase: LeaveConversationUseCase
): ViewModel() {
    companion object {
        const val ARG_DIALOG_ID = "dialog"
    }

    private val dialogId: String = savedStateHandle.get<String>(ARG_DIALOG_ID)!!
    private lateinit var loggedUser: User
    private lateinit var cachedMessages: PagingCache<Long, Message>

    private val dialog = MutableStateFlow<Dialog?>(null)
    private val messages = MutableStateFlow<PagingData<MessagePagingAdapter.MessageItem>>(PagingData.from(emptyList()))
    val uiState = combine(dialog, messages) { dialog, messages ->
        if (dialog == null)
            UiState.Loading
        else
            UiState.Loaded(
                dialogName = dialog.title,
                dialogImageUrl = dialog.image,
                messages = messages,
                isPrivate = dialog.isPrivate
            )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()

    init {
        launchHeavy {
            loggedUser = getLoggedUserProfileUseCase(Unit).asUser()
            dialog.value = getDialogByIdUseCase(dialogId)

            val pagedMessages = getPagedMessagesUseCase(dialogId)

            cachedMessages = pagedMessages.cache
            pagedMessages.data.toUi()
                .onEach { messages.emit(it) }
                .launchIn(viewModelScope)

            observeAllIncomingMessagesUseCase(Unit)
                .cancellable()
                .filter { message -> message.dialogId == dialogId }
                .collect(this@DialogViewModel::insertMessage)
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.isBlank())
            return

        launchHeavy {
            val sentMessage = sendMessageUseCase(
                SendMessageUseCase.Param(
                    message = messageText.trim(),
                    dialogId = dialogId
                )
            )
            insertMessage(sentMessage)
        }
    }

    fun leaveConversation() {
        launchHeavy {
            leaveConversationUseCase(dialogId)
            _events.emit(DataDrivenEvent.NavigateBack)
        }
    }

    fun editConversation() {
        if (dialog.value.let { it == null || it.isPrivate }) // unavailable for private dialog
            return

        launch {
            _events.emit(DataDrivenEvent.NavigateToEditConversation(dialogId))
        }
    }

    private suspend fun insertMessage(message: Message) {
        cachedMessages.addFirst(listOf(message))
        delay(100L) // FIXME dirty hack to scroll after refreshing recycler view
        _events.emit(DataDrivenEvent.ScrollToLast)
    }

    private fun Flow<PagingData<Message>>.toUi(): Flow<PagingData<MessagePagingAdapter.MessageItem>> {
        val cachedUsers = HashMap<Int, User>().apply { put(loggedUser.id, loggedUser) }
        return map { pagingData ->
            pagingData.map { message ->
                var user = cachedUsers[message.senderId]
                if (user == null) {
                    user = getUserByIdUseCase(message.senderId).user
                    cachedUsers[message.senderId] = user
                    Log.d(TAG, "User ${user.id} is cached")
                }
                message.toUi(loggedUser.id, user)
            }
        }
    }

    sealed interface UiState {
        data object Loading: UiState
        data class Loaded(
            val dialogName: String,
            val dialogImageUrl: String?,
            val messages: PagingData<MessagePagingAdapter.MessageItem>,
            val isPrivate: Boolean
        ): UiState
    }

    sealed interface DataDrivenEvent {
        data object ScrollToLast: DataDrivenEvent
        data object NavigateBack: DataDrivenEvent
        data class NavigateToEditConversation(val dialogId: String): DataDrivenEvent
    }
}