package com.vl.messenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.DialogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialogsViewModel @Inject constructor(private val dialogManager: DialogManager): ViewModel() {
    private val _dialogs = MutableStateFlow<List<User>?>(null)
    val dialogs: StateFlow<List<User>?>
        get() = _dialogs

    fun fetchDialogs() {
        viewModelScope.launch(Dispatchers.IO) {
            _dialogs.update { dialogManager.getDialogs() }
        }
    }
}