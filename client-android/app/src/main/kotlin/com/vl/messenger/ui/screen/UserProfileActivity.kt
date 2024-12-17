package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.vl.messenger.R
import com.vl.messenger.domain.entity.FriendStatus
import com.vl.messenger.ui.viewmodel.DialogViewModel
import com.vl.messenger.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileActivity: AppCompatActivity() {

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var back: ImageButton
    private lateinit var login: TextView
    private lateinit var name: TextView
    private lateinit var status: TextView
    private lateinit var image: ImageView
    private lateinit var addFriend: Button
    private lateinit var openDialog: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        back = findViewById(R.id.back)
        login = findViewById(R.id.login)
        name = findViewById(R.id.name)
        status = findViewById(R.id.status)
        image = findViewById(R.id.image)
        addFriend = findViewById(R.id.add_friend)
        openDialog = findViewById(R.id.open_dialog)

        //callbacks
        back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        addFriend.setOnClickListener {
            when (viewModel.uiState.value.availableAction) {
                UserProfileViewModel.UiState.AvailableAction.ADD_FRIEND -> viewModel.addFriend()
                UserProfileViewModel.UiState.AvailableAction.REMOVE_FRIEND -> viewModel.removeFriend()
                null -> Unit
            }
        }
        openDialog.setOnClickListener { viewModel.openDialog() }

        // subscriptions
        lifecycleScope.apply {
            launch {
                viewModel.uiState.collectLatest { state ->
                    login.text = state.name ?: getString(R.string.loading)
                    name.text = login.text
                    image.load(state.imageUrl)

                    status.text = when (state.status) {
                        FriendStatus.FRIEND -> getString(R.string.friend_status_friend)
                        FriendStatus.NONE -> getString(R.string.friend_status_none)
                        FriendStatus.REQUEST_SENT -> getString(R.string.friend_status_sent)
                        FriendStatus.REQUEST_GOTTEN -> getString(R.string.friend_status_gotten)
                        else -> getString(R.string.loading)
                    }
                    addFriend.text = when (state.availableAction) {
                        UserProfileViewModel.UiState.AvailableAction.ADD_FRIEND -> getString(R.string.add_friend)
                        UserProfileViewModel.UiState.AvailableAction.REMOVE_FRIEND -> getString(R.string.remove_friend)
                        null -> getString(R.string.loading)
                    }
                }
            }
            launch {
                viewModel.events.collect { event ->
                    when (event) {
                        is UserProfileViewModel.DataDrivenEvent.NavigateToDialog -> startActivity(
                            Intent(
                                this@UserProfileActivity,
                                DialogActivity::class.java
                            ).apply {
                                putExtra(DialogViewModel.ARG_DIALOG_ID, event.dialogId)
                            }
                        )
                    }
                }
            }
        }
    }
}