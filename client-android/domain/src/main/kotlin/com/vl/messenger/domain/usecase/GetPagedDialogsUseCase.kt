package com.vl.messenger.domain.usecase

import androidx.paging.PagingData
import com.vl.messenger.domain.boundary.DialogDataSource
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.ExtendedDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class GetPagedDialogsUseCase(
    private val sessionStore: SessionStore,
    private val dialogDataSource: DialogDataSource
): FlowUseCase<Unit, PagingData<ExtendedDialog>> {
    override fun invoke(param: Unit): Flow<PagingData<ExtendedDialog>> =
        dialogDataSource.getDialogs(runBlocking { sessionStore.getToken()!!.token })
}