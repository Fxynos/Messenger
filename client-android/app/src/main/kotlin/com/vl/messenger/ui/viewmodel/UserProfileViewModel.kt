package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.data.entity.FriendStatus
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.entity.UserProfile
import com.vl.messenger.data.manager.ProfileManager
import com.vl.messenger.data.manager.SearchManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val searchManager: SearchManager,
    private val profileManager: ProfileManager
): ViewModel() {
    private val _profile: MutableStateFlow<UserProfile?> = MutableStateFlow(null)
    val profile: StateFlow<UserProfile?>
        get() = _profile

    val ownProfile: StateFlow<User?> = flow { emit(profileManager.getProfile()) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            _profile.update {
                searchManager.getUser(user.id)
            }
        }
    }

    fun addFriend() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = profile.value!!
            profileManager.addFriend(profile.id)
            val newFriendStatus = when (profile.friendStatus) {
                FriendStatus.REQUEST_GOTTEN -> FriendStatus.FRIEND
                FriendStatus.NONE -> FriendStatus.REQUEST_SENT
                else -> null
            }
            if (newFriendStatus != null) _profile.update { UserProfile(
                profile.id,
                profile.login,
                profile.imageUrl,
                newFriendStatus
            ) }
        }
    }

    fun removeFriend() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = profile.value!!
            profileManager.removeFriend(profile.id)
            val newFriendStatus = when (profile.friendStatus) {
                FriendStatus.REQUEST_SENT -> FriendStatus.NONE
                FriendStatus.FRIEND -> FriendStatus.NONE
                else -> null
            }
            if (newFriendStatus != null) _profile.update { UserProfile(
                profile.id,
                profile.login,
                profile.imageUrl,
                newFriendStatus
            ) }
        }
    }
}