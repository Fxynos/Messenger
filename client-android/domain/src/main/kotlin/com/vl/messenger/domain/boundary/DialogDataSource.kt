package com.vl.messenger.domain.boundary

import androidx.paging.PagingData
import com.vl.messenger.domain.entity.ConversationMember
import com.vl.messenger.domain.entity.Dialog
import com.vl.messenger.domain.entity.ExtendedDialog
import kotlinx.coroutines.flow.Flow

interface DialogDataSource {
    fun getDialogs(token: String): Flow<PagingData<ExtendedDialog>>
    fun getMembers(token: String, dialogId: String): Flow<PagingData<ConversationMember>>
}