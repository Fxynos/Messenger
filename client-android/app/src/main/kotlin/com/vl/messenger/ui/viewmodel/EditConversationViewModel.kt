package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vl.messenger.domain.entity.ConversationMember
import com.vl.messenger.domain.entity.Dialog
import com.vl.messenger.domain.usecase.GetDialogByIdUseCase
import com.vl.messenger.domain.usecase.GetPagedConversationMembersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDialogByIdUseCase: GetDialogByIdUseCase,
    private val getPagedConversationMembersUseCase: GetPagedConversationMembersUseCase
): ViewModel() {
    companion object {
        const val ARG_DIALOG_ID = "dialogId"
    }

    /* Internal State */
    private val dialogId: String = savedStateHandle[ARG_DIALOG_ID]!!
    private val dialog = MutableStateFlow<Dialog?>(null)
    private val members = MutableStateFlow<PagingData<ConversationMember>>(PagingData.empty())

    private val _events = MutableSharedFlow<DataDrivenEvent>()
    val events = _events.asSharedFlow()
    val uiState = combine(dialog, members) { dialog, members ->
        UiState(
            name = dialog?.title,
            imageUrl = dialog?.image,
            members = members
        )
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dialog.value = getDialogByIdUseCase(dialogId)
            getPagedConversationMembersUseCase(dialogId)
                .cachedIn(viewModelScope)
                .collectLatest(members::emit)
        }
    }

    fun closeScreen() {
        viewModelScope.launch {
            _events.emit(DataDrivenEvent.NavigateBack(dialogId))
        }
    }

    fun showMemberOptions(member: ConversationMember) {
        TODO()
    }

    data class UiState(
        val name: String?,
        val imageUrl: String?,
        val members: PagingData<ConversationMember>
    )

    sealed interface DataDrivenEvent {
        data class NavigateBack(val dialogId: String): DataDrivenEvent
    }
}