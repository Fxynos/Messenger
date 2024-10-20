package com.vl.messenger.domain.boundary

import androidx.paging.PagingData
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.entity.VerboseUser
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    fun searchByName(token: String, pattern: String): Flow<PagingData<User>>
}