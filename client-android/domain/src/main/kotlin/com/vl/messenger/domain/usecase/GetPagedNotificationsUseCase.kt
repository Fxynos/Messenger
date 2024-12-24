package com.vl.messenger.domain.usecase

import androidx.paging.PagingData
import com.vl.messenger.domain.boundary.NotificationDataSource
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.Notification
import kotlinx.coroutines.flow.Flow

class GetPagedNotificationsUseCase(
    private val sessionStore: SessionStore,
    private val notificationDataSource: NotificationDataSource
): FlowUseCase<Unit, PagingData<Notification>> {
    override fun invoke(param: Unit): Flow<PagingData<Notification>> =
        notificationDataSource.getNotifications(sessionStore.requireToken().token)
}