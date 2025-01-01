package com.vl.messenger.domain.boundary

import androidx.paging.PagingData
import com.vl.messenger.domain.entity.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationDataSource {
    fun getNotifications(token: String): Flow<PagingData<Notification>>
}