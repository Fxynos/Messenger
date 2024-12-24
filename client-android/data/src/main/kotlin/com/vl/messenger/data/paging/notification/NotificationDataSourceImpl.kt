package com.vl.messenger.data.paging.notification

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vl.messenger.data.paging.shared.ItemKeyedPagingSource
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.NotificationDataSource
import com.vl.messenger.domain.entity.Notification
import kotlinx.coroutines.flow.Flow

class NotificationDataSourceImpl(
    private val api: MessengerRestApi
): NotificationDataSource {
    companion object {
        const val PAGE_SIZE = 10
    }

    override fun getNotifications(token: String): Flow<PagingData<Notification>> {
        val request: suspend (key: Long?, limit: Int) -> List<Notification> = { key, limit ->
            api.getNotifications(token, limit, key)
        }
        return Pager(
            config = PagingConfig(PAGE_SIZE),
            pagingSourceFactory = { ItemKeyedPagingSource(request, Notification::id) }
        ).flow
    }
}