package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.vl.messenger.R
import com.vl.messenger.databinding.ActivityEditConversationBinding
import com.vl.messenger.ui.adapter.ConversationMemberPagingAdapter
import com.vl.messenger.ui.modal.dropPopupOptions
import com.vl.messenger.ui.modal.dropSelectUserDialog
import com.vl.messenger.ui.viewmodel.DialogViewModel
import com.vl.messenger.ui.viewmodel.EditConversationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditConversationActivity: AppCompatActivity() {

    private val viewModel: EditConversationViewModel by viewModels()
    private lateinit var binding: ActivityEditConversationBinding
    private lateinit var adapter: ConversationMemberPagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ConversationMemberPagingAdapter(this, viewModel::showMemberOptions)
        binding = ActivityEditConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            members.adapter = adapter
            options.setOnClickListener { showPopupOptions() }
            back.setOnClickListener { viewModel.closeScreen() }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.closeScreen()
            }
        })

        lifecycleScope.launch {
            launch { viewModel.uiState.collectLatest(::updateState) }
            launch { viewModel.events.collect(::handleEvent) }
        }
    }

    private suspend fun updateState(state: EditConversationViewModel.UiState) {
        with(binding) {
            name.text = state.name
            image.load(state.imageUrl)
            adapter.submitData(state.members)
        }
    }

    private fun handleEvent(event: EditConversationViewModel.DataDrivenEvent) {
        when (event) {
            is EditConversationViewModel.DataDrivenEvent.NavigateBack -> {
                startActivity(Intent(this, DialogActivity::class.java).apply {
                    putExtra(DialogViewModel.ARG_DIALOG_ID, event.dialogId)
                })
                finish()
            }

            is EditConversationViewModel.DataDrivenEvent.ShowFriendsToInviteDialog -> dropSelectUserDialog(
                title = R.string.dialog_invite_title,
                items = event.users,
                onSelect = viewModel::inviteMember
            )

            is EditConversationViewModel.DataDrivenEvent.NotifyMemberAdded -> Toast.makeText(
                this,
                getString(R.string.dialog_member_invited, event.member.login),
                Toast.LENGTH_LONG
            ).show()

            is EditConversationViewModel.DataDrivenEvent.ShowMemberOptions ->
                binding.members.dropPopupOptions(
                    *buildList {
                        if (event.canBeRemoved)
                            add(R.string.member_option_remove to Runnable {
                                viewModel.removeMember(event.member)
                            })

                        if (event.canRoleBeAssigned)
                            add(R.string.member_option_assign_role to Runnable {
                                viewModel.selectRole(event.member)
                            })
                    }.toTypedArray()
                )
        }
    }

    private fun showPopupOptions() {
        binding.options.dropPopupOptions(
            R.string.dialog_option_invite to Runnable { viewModel.selectMemberToInvite() }
        )
    }
}