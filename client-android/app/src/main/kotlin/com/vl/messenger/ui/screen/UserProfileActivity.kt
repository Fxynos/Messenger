package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.vl.messenger.R
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.data.entity.FriendStatus
import com.vl.messenger.data.entity.User
import com.vl.messenger.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class UserProfileActivity: AppCompatActivity(), View.OnClickListener {

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

        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra("user", User::class.java)!!
        else
            intent.getParcelableExtra("user")!!

        back = findViewById(R.id.back)
        login = findViewById(R.id.login)
        name = findViewById(R.id.name)
        status = findViewById(R.id.status)
        image = findViewById(R.id.image)
        addFriend = findViewById(R.id.add_friend)
        openDialog = findViewById(R.id.open_dialog)
        back.setOnClickListener(this)
        addFriend.setOnClickListener(this)
        openDialog.setOnClickListener(this)
        image.load(user.imageUrl)

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.profile.collect {
                val profile = it ?: user
                val friendStatus = it?.friendStatus

                login.text = profile.login
                name.text = profile.login
                status.text = when (friendStatus) {
                    FriendStatus.FRIEND -> getString(R.string.friend_status_friend)
                    FriendStatus.NONE -> getString(R.string.friend_status_none)
                    FriendStatus.REQUEST_SENT -> getString(R.string.friend_status_sent)
                    FriendStatus.REQUEST_GOTTEN -> getString(R.string.friend_status_gotten)
                    null -> ""
                }
                addFriend.isEnabled = friendStatus != null
                addFriend.text = when (friendStatus) {
                    FriendStatus.NONE, FriendStatus.REQUEST_GOTTEN, null -> getString(R.string.add_friend)
                    FriendStatus.FRIEND, FriendStatus.REQUEST_SENT -> getString(R.string.remove_friend)
                }
            }
        }

        viewModel.updateUser(user) // fetch friend status
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.back -> onBackPressedDispatcher.onBackPressed()
            R.id.add_friend -> when (viewModel.profile.value!!.friendStatus) {
                FriendStatus.NONE, FriendStatus.REQUEST_GOTTEN -> viewModel.addFriend()
                FriendStatus.FRIEND, FriendStatus.REQUEST_SENT -> viewModel.removeFriend()
            }
            R.id.open_dialog -> startActivity(
                Intent(this, DialogActivity::class.java).apply {
                    putExtra(DialogActivity.EXTRA_OWN_ID, runBlocking { getOwnUserId() })
                    putExtra(DialogActivity.EXTRA_DIALOG, runBlocking {
                        val user = getUser()
                        Dialog(
                            user.id.toLong(),
                            true,
                            user.login,
                            user.imageUrl,
                            null,
                            null
                        )
                    })
                }
            )
        }
    }

    private suspend fun getOwnUserId() =
        viewModel.ownProfile.filterNotNull().first().id

    private suspend fun getUser() =
        viewModel.profile.filterNotNull().first()
}