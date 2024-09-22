package com.vl.messenger.data.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.data.entity.Message
import java.text.SimpleDateFormat
import java.util.Locale

private const val VIEW_TYPE_SENT = 1
private const val VIEW_TYPE_RECEIVED = 2

class PrivateMessagePagingAdapter(
    context: Context,
    private val ownUserId: Int
): PagingDataAdapter<Message, PrivateMessagePagingAdapter.ViewHolder>(
    PrivateMessagePagingAdapter
) {
    companion object: DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Message, newItem: Message) =
            oldItem.content == newItem.content
                    && oldItem.timestamp == newItem.timestamp
                    && oldItem.senderId == newItem.senderId
    }

    private val layoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)!!)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        layoutInflater.inflate(when (viewType) {
            VIEW_TYPE_SENT -> R.layout.item_unsigned_message_sent
            VIEW_TYPE_RECEIVED -> R.layout.item_unsigned_message_received
            else -> throw RuntimeException() // unreachable
        }, parent, false).let(::ViewHolder)

    override fun getItemViewType(position: Int) =
        if (getItem(position)!!.senderId == ownUserId)
            VIEW_TYPE_SENT
        else
            VIEW_TYPE_RECEIVED

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val content: TextView = view.findViewById(R.id.content)
        private val time: TextView = view.findViewById(R.id.time)

        fun bind(item: Message) {
            content.text = item.content
            time.text = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(item.timestamp * 1000)
        }
    }
}