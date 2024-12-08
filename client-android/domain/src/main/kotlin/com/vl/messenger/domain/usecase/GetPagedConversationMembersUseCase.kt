package com.vl.messenger.domain.usecase

import androidx.paging.PagingData
import com.vl.messenger.domain.boundary.DialogDataSource
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.ConversationMember
import com.vl.messenger.domain.entity.Dialog
import kotlinx.coroutines.flow.Flow

class GetPagedConversationMembersUseCase(
    private val sessionStore: SessionStore,
    private val dialogDataSource: DialogDataSource
): FlowUseCase<String, PagingData<ConversationMember>> {
    /**
     * @param param dialog id
     */
    override fun invoke(param: String): Flow<PagingData<ConversationMember>> =
        dialogDataSource.getMembers(sessionStore.getTokenBlocking()!!.token, param)
}