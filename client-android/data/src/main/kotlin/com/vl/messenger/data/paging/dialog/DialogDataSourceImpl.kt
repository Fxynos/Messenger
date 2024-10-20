package com.vl.messenger.data.paging.dialog

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vl.messenger.data.paging.shared.OffsetKeyedPagingSource
import com.vl.messenger.domain.boundary.DialogDataSource
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.entity.ExtendedDialog
import kotlinx.coroutines.flow.Flow

class DialogDataSourceImpl(
    private val api: MessengerRestApi
): DialogDataSource {
    companion object {
        private const val PAGE_SIZE = 10
    }

    override fun getDialogs(token: String): Flow<PagingData<ExtendedDialog>> {
        val request: suspend (offset: Int, limit: Int) -> List<ExtendedDialog> = { key, limit ->
            api.getDialogs(token, limit, key)
        }
        return Pager(
            config = PagingConfig(PAGE_SIZE),
            pagingSourceFactory = { OffsetKeyedPagingSource(request) }
        ).flow
    }
}