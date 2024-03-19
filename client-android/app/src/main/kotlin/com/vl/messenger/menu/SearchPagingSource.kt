package com.vl.messenger.menu

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vl.messenger.menu.entity.User

class SearchPagingSource(
    private val searchManager: SearchManager,
    private val pattern: String
): PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>) = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> = try {
        val users = searchManager.search(pattern, params.loadSize, params.key)
        LoadResult.Page(users, null, users.takeIf { it.size == params.loadSize }?.last()?.id)
    } catch (exception: Exception) {
        exception.printStackTrace()
        LoadResult.Error(exception)
    }
}