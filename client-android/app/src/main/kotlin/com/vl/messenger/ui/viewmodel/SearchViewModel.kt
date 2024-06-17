package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vl.messenger.data.manager.SearchManager
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.component.SearchPagingSource
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