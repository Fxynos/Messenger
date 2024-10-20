package com.vl.messenger.data.paging.user

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vl.messenger.data.paging.shared.ItemKeyedPagingSource
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.UserDataSource
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.entity.VerboseUser
import kotlinx.coroutines.flow.Flow

class UserDataSourceImpl(
    private val api: MessengerRestApi
): UserDataSource {
    companion object {
        private const val PAGE_SIZE = 10
    }

    override fun searchByName(token: String, pattern: String): Flow<PagingData<User>> {
        val request: suspend (key: Int?, limit: Int) -> List<User> = { key, limit ->
            api.searchUserByName(token, pattern, limit, key)
                .map(VerboseUser::user)
        }
        return Pager(
            config = PagingConfig(PAGE_SIZE),
            pagingSourceFactory = { ItemKeyedPagingSource(request, User::id) }
        ).flow
    }
}