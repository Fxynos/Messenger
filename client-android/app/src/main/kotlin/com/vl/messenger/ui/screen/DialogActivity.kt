package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.R
import com.vl.messenger.databinding.ActivityDialogBinding
import com.vl.messenger.ui.adapter.MessagePagingAdapter
import com.vl.messenger.ui.modal.dropConfirmationDialog
import com.vl.messenger.ui.modal.dropPopupOptions
import com.vl.messenger.ui.modal.dropSelectUserDialog
import com.vl.messenger.ui.viewmodel.DialogViewModel
import com.vl.messenger.ui.viewmodel.EditConversationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "DialogActivity"

@AndroidEntryPoint
class DialogActivity: AppCompatActivity() {

private val viewModel: DialogViewModel by viewModels()

    private lateinit var binding: ActivityDialogBinding
    private lateinit var adapter: MessagePagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MessagePagingAdapter(this)

        with(binding) {
            messages.adapter = adapter
            back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            options.setOnClickListener { showPopupOptions() }
            send.setOnClickListener {
                viewModel.sendMessage(input.text.toString())
                input.text.clear()
            }
            name.setOnClickListener { viewModel.editConversation() }
            image.setOnClickListener { viewModel.editConversation() }
        }

        // subscriptions
        lifecycleScope.launch(Dispatchers.Main) {
            launch { viewModel.uiState.collect(this@DialogActivity::updateState) }
            launch { viewModel.events.collect(this@DialogActivity::handleEvent) }
            launch { adapter.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.refresh != LoadState.Loading)
                    binding.noMessagesHint.isVisible = adapter.itemCount == 0
            } }
        }
    }

    private suspend fun updateState(state: DialogViewModel.UiState) {
        Log.d(TAG, "UI State: $state")
        when (state) {
            is DialogViewModel.UiState.Loading -> with(binding) {
                name.text = getString(R.string.loading)
                image.setImageBitmap(null)
            }
            is DialogViewModel.UiState.Loaded -> with(binding) {
                name.text = state.dialogName
                image.load(state.dialogImageUrl)
                adapter.submitData(state.messages)
            }
        }
    }

    private suspend fun handleEvent(event: DialogViewModel.DataDrivenEvent) {
        Log.d(TAG, "Event: $event")
        when (event) {
            DialogViewModel.DataDrivenEvent.ScrollToLast -> {
                val layoutManager = binding.messages.layoutManager as LinearLayoutManager
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

                if ( // scroll down to new message
                    binding.messages.scrollState == RecyclerView.SCROLL_STATE_IDLE &&
                    firstVisiblePosition == 1
                ) withContext(Dispatchers.Main) {
                    binding.messages.smoothScrollToPosition(0)
                }
            }

            DialogViewModel.DataDrivenEvent.NavigateBack -> finish()

            is DialogViewModel.DataDrivenEvent.NavigateToEditConversation -> {
                startActivity(Intent(this, EditConversationActivity::class.java).apply {
                    putExtra(EditConversationViewModel.ARG_DIALOG_ID, event.dialogId)
                })
                finish()
            }
        }
    }

    private fun showPopupOptions() {
        val dialog = (viewModel.uiState.value as? DialogViewModel.UiState.Loaded)

        binding.options.dropPopupOptions(*buildList {
            if (dialog != null && !dialog.isPrivate) { // conversation-only
                add(R.string.dialog_option_edit to Runnable { viewModel.editConversation() })
                add(R.string.dialog_option_leave to Runnable {
                    dropConfirmationDialog(
                        title = R.string.dialog_option_leave_title,
                        message = R.string.dialog_option_leave_message,
                        cancel = R.string.dialog_option_leave_cancel,
                        confirm = R.string.dialog_option_leave_ok,
                        onConfirm = viewModel::leaveConversation
                    )
                })
            }
        }.toTypedArray())
    }
}