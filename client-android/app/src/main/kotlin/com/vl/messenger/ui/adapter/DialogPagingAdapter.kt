package com.vl.messenger.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.R
import com.vl.messenger.databinding.ItemUserBinding
import com.vl.messenger.ui.entity.DialogUi

class DialogPagingAdapter(
    private val context: Context,
    private val onClick: (DialogUi) -> Unit
): PagingDataAdapter<DialogUi, DialogPagingAdapter.ViewHolder>(DialogPagingAdapter) {

    companion object: DiffUtil.ItemCallback<DialogUi>() {
        override fun areItemsTheSame(oldItem: DialogUi, newItem: DialogUi): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DialogUi, newItem: DialogUi): Boolean =
            oldItem == newItem
    }

    private val inflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)!!)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemUserBinding.inflate(inflater, parent, false))

    inner class ViewHolder(
        private val binding: ItemUserBinding
    ): RecyclerView.ViewHolder(binding.root) {

        private var item: DialogUi? = null

        init {
            binding.root.setOnClickListener { item?.apply(this@DialogPagingAdapter.onClick) }
        }

        fun bind(item: DialogUi) {
            this.item = item
            with(binding) {
                conversationMarker.isVisible = !item.isPrivate
                title.text = item.name
                text.text = item.lastMessageText
                hint.isVisible = item.lastMessageSenderName != null
                if (!item.lastMessageSenderName.isNullOrBlank())
                    hint.text = context.getString(
                        R.string.dialog_last_message_sender,
                        item.lastMessageSenderName
                    )

                image.load(item.imageUrl)
            }
        }
    }
}