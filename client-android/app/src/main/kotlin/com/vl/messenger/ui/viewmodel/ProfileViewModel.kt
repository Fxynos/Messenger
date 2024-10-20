package com.vl.messenger.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    app: Application,
    private val sessionStore: SessionStore,
    private val profileManager: ProfileManager,
    downloadManager: DownloadManager
): AndroidViewModel(app) {
    private val context: Context
        get() = getApplication()

    private val _profile: MutableStateFlow<User?> = MutableStateFlow(null)
    val profile: StateFlow<User?> get() = _profile

    private val _profileImage: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val profileImage: StateFlow<Bitmap?> get() = _profileImage

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchProfile()
            val imageUrl = _profile.value!!.imageUrl
            if (imageUrl != null) _profileImage.update { downloadManager.downloadBitmap(imageUrl) }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            sessionStore.removeAccessToken()
        }
    }

    fun uploadPhoto(imageUri: Uri) {
        val stream = context.contentResolver.openInputStream(imageUri)!!
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeStream(stream)
            profileManager.uploadPhoto(bitmap)
            fetchProfile()
            _profileImage.update { bitmap }
        }
    }

    /**
     * Blocking
     */
    private fun fetchProfile() {
        _profile.update {
            profileManager.getProfile()
        }
    }
}