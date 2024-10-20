package com.vl.messenger.domain.entity

import androidx.paging.PagingData
import com.vl.messenger.domain.boundary.PagingCache
import kotlinx.coroutines.flow.Flow

data class CachedPagingData<K, V: Any>(
    val cache: PagingCache<K, V>,
    val data: Flow<PagingData<V>>
)