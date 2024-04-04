package com.vl.messenger.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.data.manager.ProfileManager
import com.vl.messenger.data.manager.SessionStore
import com.vl.messenger.data.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    profileManager: ProfileManager,
    downloadManager: DownloadManager
): ViewModel() {
    val profile: Flow<User> = flow {
        emit(profileManager.getProfile())
    }.flowOn(Dispatchers.IO)

    val profileImage: Flow<Bitmap?> = profile.map {
        it.imageUrl?.let(downloadManager::downloadBitmap)
    }.flowOn(Dispatchers.IO)

    fun logOut() {
        viewModelScope.launch {
            sessionStore.removeAccessToken()
        }
    }
}