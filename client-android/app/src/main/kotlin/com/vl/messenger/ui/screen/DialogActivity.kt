package com.vl.messenger.ui.screen

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.R
import com.vl.messenger.databinding.ActivityDialogBinding
import com.vl.messenger.ui.adapter.MessagePagingAdapter
import com.vl.messenger.ui.viewmodel.DialogViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            options.setOnClickListener { /* TODO */ }
            send.setOnClickListener {
                viewModel.sendMessage(input.text.toString())
                input.text.clear()
            }
        }

        // subscriptions
        lifecycleScope.launch(Dispatchers.Main) {
            launch { viewModel.uiState.collect(this@DialogActivity::updateState) }
            launch { viewModel.events.collect(this@DialogActivity::handleEvent) }
        }
    }

    private suspend fun updateState(state: DialogViewModel.UiState) {
        when (state) {
            is DialogViewModel.UiState.Loading -> with(binding) {
                name.text = getString(R.string.loading)
                icon.setImageBitmap(null)
                noMessagesHint.isVisible = false
            }
            is DialogViewModel.UiState.Loaded -> with(binding) {
                name.text = state.dialogName
                icon.load(state.dialogImageUrl)
                adapter.submitData(state.messages)
                noMessagesHint.isVisible = adapter.itemCount == 0
            }
        }
    }

    private suspend fun handleEvent(event: DialogViewModel.DataDrivenEvent) {
        when (event) {
            DialogViewModel.DataDrivenEvent.RefreshMessages -> adapter.refresh()

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
        }
    }
}