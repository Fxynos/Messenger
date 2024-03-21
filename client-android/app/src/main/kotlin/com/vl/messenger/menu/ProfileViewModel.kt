package com.vl.messenger.menu

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.ProfileManager
import com.vl.messenger.auth.SessionStore
import com.vl.messenger.menu.entity.User
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