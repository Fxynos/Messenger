package com.vl.messenger.domain.usecase.param

sealed interface DialogParam {
    data class PrivateDialog(val userId: Int): DialogParam
    data class Conversation(val conversationId: Long): DialogParam
}