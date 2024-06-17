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
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.data.component.PrivateMessagesPagingAdapter
import com.vl.messenger.data.entity.Conversation
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.data.entity.PrivateDialog
import com.vl.messenger.ui.viewmodel.DialogViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Accepts extras:
 * - [EXTRA_PRIVATE_DIALOG] or [EXTRA_CONVERSATION]
 * - [EXTRA_OWN_ID]
 */
@AndroidEntryPoint
class DialogActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_PRIVATE_DIALOG = "dialog"
        const val EXTRA_CONVERSATION = "conversation"
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

    private lateinit var adapter: PrivateMessagesPagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        /* Args */
        val userId: Int = intent.getIntExtra(EXTRA_OWN_ID, -1).takeUnless { it == -1 }!!
        val dialog: Dialog = intent.getParcelableExtra<PrivateDialog>(EXTRA_PRIVATE_DIALOG)
            ?: intent.getParcelableExtra<Conversation>(EXTRA_CONVERSATION)!!

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

        adapter = PrivateMessagesPagingAdapter(this, userId)
        messagesList.adapter = adapter

        back.setOnClickListener(this::onClick)
        options.setOnClickListener(this::onClick)
        send.setOnClickListener(this::onClick)

        noMessages.visibility = View.GONE // TODO

        /* State */
        lifecycleScope.launch(Dispatchers.Main) {
            launch { viewModel.messages.collect(adapter::submitData) }
            launch { viewModel.uiState.collect(this@DialogActivity::updateState) }
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
                image.setImageBitmap(state.dialogImage)
            }
        }
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.back -> onBackPressedDispatcher.onBackPressed()
            R.id.options -> TODO()
            R.id.send -> TODO()
        }
    }
}