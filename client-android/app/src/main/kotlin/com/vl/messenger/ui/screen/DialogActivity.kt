package com.vl.messenger.ui.screen

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.R
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.ui.adapter.MessageListPagedAdapter
import com.vl.messenger.ui.viewmodel.DialogViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Accepts extras:
 * - [EXTRA_DIALOG]
 * - [EXTRA_OWN_ID]
 */
@AndroidEntryPoint
class DialogActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_DIALOG = "dialog"
        const val EXTRA_OWN_ID = "id" // own user id
    }

    private val viewModel: DialogViewModel by viewModels()

    private lateinit var back: ImageButton
    private lateinit var options: ImageButton
    private lateinit var send: ImageButton
    private lateinit var image: ImageView
    private lateinit var input: EditText
    private lateinit var name: TextView
    private lateinit var noMessages: TextView

    private lateinit var adapter: MessageListPagedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        /* Args */
        val userId: Int = intent.getIntExtra(EXTRA_OWN_ID, -1).takeUnless { it == -1 }!!
        val dialog: Dialog = intent.getParcelableExtra(EXTRA_DIALOG)!!

        viewModel.initialize(dialog)

        /* Views */
        back = findViewById(R.id.back)
        options = findViewById(R.id.options)
        send = findViewById(R.id.send)
        input = findViewById(R.id.input)
        image = findViewById(R.id.icon)
        name = findViewById(R.id.name)
        noMessages = findViewById(R.id.no_messages_hint)
        val messagesList = findViewById<RecyclerView>(R.id.messages)

        adapter = MessageListPagedAdapter()
        messagesList.adapter = adapter

        back.setOnClickListener(this::onClick)
        options.setOnClickListener(this::onClick)
        send.setOnClickListener(this::onClick)

        noMessages.visibility = View.GONE // TODO

        /* State */
        lifecycleScope.launch(Dispatchers.Main) {
            launch { viewModel.messages.collect(adapter::submitList) }
            launch { viewModel.uiState.collect(this@DialogActivity::updateState) }
            launch { viewModel.scrollEvents.collect {
                val layoutManager = messagesList.layoutManager as LinearLayoutManager
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

                if ( // scroll down to new message
                    messagesList.scrollState == RecyclerView.SCROLL_STATE_IDLE &&
                    firstVisiblePosition == 1
                ) withContext(Dispatchers.Main) {
                    messagesList.smoothScrollToPosition(0)
                }
            } }
        }
    }

    private fun updateState(state: DialogViewModel.UiState) {
        when (state) {
            is DialogViewModel.UiState.Loading -> {
                name.text = getString(R.string.loading)
                image.setImageBitmap(null)
            }
            is DialogViewModel.UiState.DialogShown -> {
                name.text = state.dialogName
                image.load(state.dialogImage)
            }
        }
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.back -> onBackPressedDispatcher.onBackPressed()
            R.id.options -> TODO()
            R.id.send -> {
                viewModel.send(input.text.toString())
                input.text.clear()
            }
        }
    }
}