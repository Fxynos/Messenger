package com.vl.messenger.data.component

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.R
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.domain.ListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DialogViewHolder(
    private val context: Context,
    private val adapter: ListAdapter<Dialog>,
    view: View,
    private val ownUserId: Int
): RecyclerView.ViewHolder(view), View.OnClickListener {

    private val title: TextView = view.findViewById(R.id.title)
    private val hint: TextView = view.findViewById(R.id.hint)
    private val text: TextView = view.findViewById(R.id.text)
    private val image: ImageView = view.findViewById(R.id.image)
    private val conversationMarker: ImageView = view.findViewById(R.id.conversation_marker)

    private lateinit var dialog: Dialog

    init { view.setOnClickListener(this) }

    fun bind(dialog: Dialog) {
        this.dialog = dialog
        conversationMarker.visibility = if (dialog.isPrivate) View.GONE else View.VISIBLE
        hint.visibility =
            if (!dialog.isPrivate || dialog.lastMessage?.senderId == ownUserId)
                View.VISIBLE
            else
                View.GONE
        hint.text =
            if (dialog.isPrivate || dialog.lastMessageSender == null || dialog.lastMessageSender.id == ownUserId)
                context.getString(R.string.you)
            else
                "${dialog.lastMessageSender.login}:"
        text.text = dialog.lastMessage?.content ?: ""
        title.text = dialog.title
        image.load(dialog.image)
    }

    override fun onClick(view: View) {
        adapter.onItemClickListener?.onClick(dialog, absoluteAdapterPosition)
    }
}