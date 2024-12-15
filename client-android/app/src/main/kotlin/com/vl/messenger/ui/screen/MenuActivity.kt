package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.vl.messenger.R
import com.vl.messenger.databinding.ActivityMenuBinding
import com.vl.messenger.databinding.ItemUserBinding
import com.vl.messenger.ui.modal.dropPopupOptions
import com.vl.messenger.ui.modal.dropTextInputDialog
import com.vl.messenger.ui.viewmodel.DialogViewModel
import com.vl.messenger.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuActivity: AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ActivityMenuBinding
    private lateinit var drawerHeaderBinding: ItemUserBinding
    private val routes: Map<Int, Class<out Fragment>> = mapOf(
        R.id.search to SearchFragment::class.java,
        R.id.friends to FriendsFragment::class.java,
        R.id.dialogs to DialogsFragment::class.java
    )
    private lateinit var pickMediaRequestLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        drawerHeaderBinding = ItemUserBinding.bind(binding.navigation.getHeaderView(0))
        setContentView(binding.root)

        pickMediaRequestLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            if (it == null)
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
            else
                viewModel.updatePhoto(it)
        }

        // callbacks
        binding.navigation.setNavigationItemSelectedListener { menuItem ->
            binding.drawer.closeDrawer(GravityCompat.START)
            if (menuItem.groupId == R.id.navigation)
                navigateTo(menuItem.itemId)
            else when (menuItem.itemId) {
                R.id.conversation -> showCreateConversationDialog()
                R.id.logout -> viewModel.logOut()
            }
            true
        }
        drawerHeaderBinding.image.setOnClickListener { showPopupProfileMenu() }

        // setup nav
        binding.navigation.setCheckedItem(R.id.dialogs)
        navigateTo(R.id.dialogs)

        // subscriptions
        lifecycleScope.apply {
            launch { viewModel.uiState.collectLatest(this@MenuActivity::updateState) }
            launch { viewModel.events.collect(this@MenuActivity::handleEvent) }
        }
    }

    fun openDrawer(): Unit = binding.drawer.openDrawer(GravityCompat.START)

    private fun updateState(state: ProfileViewModel.UiState) {
        when (state) {
            is ProfileViewModel.UiState.Loaded -> {
                with(drawerHeaderBinding) {
                    image.load(state.imageUrl)
                    title.text = state.name
                }
            }

            ProfileViewModel.UiState.Loading -> {
                with(drawerHeaderBinding) {
                    image.setImageBitmap(null)
                    title.text = getString(R.string.loading)
                }
            }
        }
    }

    private fun handleEvent(event: ProfileViewModel.DataDrivenEvent) {
        when (event) {
            ProfileViewModel.DataDrivenEvent.NavigateToAuthScreen -> {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }

            is ProfileViewModel.DataDrivenEvent.NavigateToDialog ->
                startActivity(Intent(this, DialogActivity::class.java).apply {
                    putExtra(DialogViewModel.ARG_DIALOG_ID, event.dialogId)
                })

            ProfileViewModel.DataDrivenEvent.NotifyCreatingConversationFailed ->
                Toast.makeText(
                    this,
                    R.string.dialog_create_conversation_unknown_error,
                    Toast.LENGTH_LONG
                ).show()
        }
    }

    private fun navigateTo(@IdRes route: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.placeholder, routes[route]!!, null)
            .commit()
    }

    private fun showPopupProfileMenu() {
        drawerHeaderBinding.image.dropPopupOptions(

            R.string.upload_photo to Runnable {
                pickMediaRequestLauncher.launch(PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                ))
            },

            if ((viewModel.uiState.value as? ProfileViewModel.UiState.Loaded)?.isUserHidden == true)
                R.string.make_profile_visible to Runnable {
                    AlertDialog.Builder(this@MenuActivity)
                        .setTitle(R.string.dialog_open_profile_title)
                        .setMessage(R.string.dialog_open_profile_message)
                        .setPositiveButton(R.string.dialog_open_profile_button_ok) { _, _ ->
                            viewModel.setProfileHidden(false)
                        }
                        .setNegativeButton(R.string.dialog_open_profile_button_cancel, null)
                        .show()
                }
            else
                R.string.make_profile_hidden to Runnable {
                    AlertDialog.Builder(this@MenuActivity)
                        .setTitle(R.string.dialog_hide_profile_title)
                        .setMessage(R.string.dialog_hide_profile_message)
                        .setPositiveButton(R.string.dialog_hide_profile_button_ok) { _, _ ->
                            viewModel.setProfileHidden(true)
                        }
                        .setNegativeButton(R.string.dialog_hide_profile_button_cancel, null)
                        .show()
                }
        )
    }

    private fun showCreateConversationDialog() {
        dropTextInputDialog(
            R.string.dialog_create_conversation_title,
            R.string.dialog_create_conversation_input_hint,
            R.string.dialog_create_conversation_ok,
            R.string.dialog_create_conversation_cancel
        ) {
            viewModel.createConversation(it)
        }
    }
}