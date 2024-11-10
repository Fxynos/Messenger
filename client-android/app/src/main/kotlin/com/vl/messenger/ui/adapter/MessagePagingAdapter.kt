package com.vl.messenger.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.vl.messenger.databinding.ItemSignedMessageReceivedBinding
import com.vl.messenger.databinding.ItemUnsignedMessageSentBinding
import com.vl.messenger.ui.adapter.MessagePagingAdapter.MessageItem

class MessagePagingAdapter(
    context: Context
): PagingDataAdapter<MessageItem, MessagePagingAdapter.ViewHolder<out MessageItem, out ViewBinding>>(MessagePagingAdapter) {

    companion object: DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean =
            oldItem == newItem
    }

    private val inflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: ViewHolder<*, *>, position: Int) = when (holder) {
        is ViewHolder.ReceivedMessage -> holder.bind(getItem(position) as MessageItem.Received)
        is ViewHolder.SentMessage -> holder.bind(getItem(position) as MessageItem.Sent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*, *> =
        when (ViewType.entries[viewType]) {
            ViewType.SENT -> ViewHolder.SentMessage(
                ItemUnsignedMessageSentBinding.inflate(inflater, parent, false)
            )
            ViewType.RECEIVED -> ViewHolder.ReceivedMessage(
                ItemSignedMessageReceivedBinding.inflate(inflater, parent, false)
            )
        }

    override fun getItemViewType(position: Int): Int = getItem(position)!!.viewType.ordinal

    /* Entities */

    enum class ViewType {
        SENT,
        RECEIVED
    }

    sealed class ViewHolder<T: MessageItem, VB: ViewBinding>(
        protected val binding: VB
    ): RecyclerView.ViewHolder(binding.root) {

        abstract fun bind(item: T)

        class SentMessage(
            binding: ItemUnsignedMessageSentBinding
        ): ViewHolder<MessageItem.Sent, ItemUnsignedMessageSentBinding>(binding) {
            override fun bind(item: MessageItem.Sent) {
                with(binding) {
                    content.text = item.text
                    time.text = item.time
                }
            }
        }

        class ReceivedMessage(
            binding: ItemSignedMessageReceivedBinding
        ): ViewHolder<MessageItem.Received, ItemSignedMessageReceivedBinding>(binding) {
            override fun bind(item: MessageItem.Received) {
                with(binding) {
                    content.text = item.text
                    time.text = item.time
                    name.text = item.senderName
                    image.load(item.senderImageUrl)
                }
            }
        }
    }

    sealed interface MessageItem {
        val id: Long
        val text: String
        val time: String
        val viewType: ViewType

        data class Sent(
            override val id: Long,
            override val text: String,
            override val time: String
        ): MessageItem {
            override val viewType = ViewType.SENT
        }

        data class Received(
            override val id: Long,
            override val text: String,
            override val time: String,
            val senderName: String,
            val senderImageUrl: String?
        ): MessageItem {
            override val viewType = ViewType.RECEIVED
        }
    }
}