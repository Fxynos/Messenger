package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.request.CachePolicy
import com.vl.messenger.R
import com.vl.messenger.databinding.ActivityEditConversationBinding
import com.vl.messenger.ui.adapter.ConversationMemberPagingAdapter
import com.vl.messenger.ui.modal.dropPopupOptions
import com.vl.messenger.ui.modal.dropSelectRoleDialog
import com.vl.messenger.ui.modal.dropSelectUserDialog
import com.vl.messenger.ui.modal.dropTextInputDialog
import com.vl.messenger.ui.utils.setOnClick
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

    private lateinit var pickMediaRequestLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ConversationMemberPagingAdapter(this, viewModel::showMemberOptions)
        binding = ActivityEditConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pickMediaRequestLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            if (it == null)
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
            else
                viewModel.setConversationImage(it)
        }

        with(binding) {
            members.adapter = adapter
            options.setOnClick(viewModel::showPopupOptions)
            back.setOnClick(viewModel::closeScreen)
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
            image.load(state.imageUrl) { memoryCachePolicy(CachePolicy.DISABLED) }
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

            is EditConversationViewModel.DataDrivenEvent.NotifyMemberRemoved -> Toast.makeText(
                this,
                getString(R.string.dialog_member_removed, event.member.user.login),
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

            is EditConversationViewModel.DataDrivenEvent.ShowRolesToSet -> dropSelectRoleDialog(
                event.member.user,
                event.roles
            ) { role ->
                viewModel.setMemberRole(event.member, role)
            }

            is EditConversationViewModel.DataDrivenEvent.NotifyMemberRoleSet -> Toast.makeText(
                this,
                getString(
                    R.string.dialog_member_role_set,
                    event.member.user.login,
                    event.member.role.name
                ),
                Toast.LENGTH_LONG
            ).show()

            is EditConversationViewModel.DataDrivenEvent.ShowPopupOptions ->
                binding.options.dropPopupOptions(*buildList {
                    if (event.canInviteMembers)
                        add(R.string.dialog_option_invite to Runnable { viewModel.selectMemberToInvite() })

                    if (event.canDownloadReports)
                        add(R.string.dialog_option_download_report to Runnable { viewModel.downloadReport() })

                    if (event.canEditName)
                        add(R.string.dialog_change_name to Runnable { showChangeNamePopup() })

                    if (event.canEditImage)
                        add(R.string.dialog_change_image to Runnable {
                            pickMediaRequestLauncher.launch(PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            ))
                        })
                }.toTypedArray())

            EditConversationViewModel.DataDrivenEvent.NotifyDownloadingReport -> Toast.makeText(
                this,
                R.string.conversation_report_downloading,
                Toast.LENGTH_LONG
            ).show()

            is EditConversationViewModel.DataDrivenEvent.NotifyReportDownloaded -> Toast.makeText(
                this,
                getString(R.string.conversation_report_downloaded, event.pathToFile),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showChangeNamePopup() {
        dropTextInputDialog(
            R.string.set_conversation_name_title,
            R.string.set_conversation_name_input_hint,
            R.string.set_conversation_name_ok,
            R.string.set_conversation_name_cancel
        ) {
            viewModel.setConversationName(it)
        }
    }
}