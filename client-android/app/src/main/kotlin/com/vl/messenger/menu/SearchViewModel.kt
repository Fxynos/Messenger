package com.vl.messenger.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vl.messenger.menu.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchManager: SearchManager
): ViewModel() {
    fun search(pattern: String): Flow<PagingData<User>> = Pager(PagingConfig(10)) {
        SearchPagingSource(searchManager, pattern)
    }.flow
}