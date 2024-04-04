package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.ProfileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(private val profileManager: ProfileManager): ViewModel() {
    private val _friends = MutableStateFlow<List<User>?>(null)
    val friends: StateFlow<List<User>?>
        get() = _friends

    fun fetchFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            _friends.update { profileManager.getFriends() }
        }
    }
}