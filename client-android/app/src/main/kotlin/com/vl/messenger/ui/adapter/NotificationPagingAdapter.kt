package com.vl.messenger.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.databinding.ItemNotificationBinding
import com.vl.messenger.domain.entity.Notification
import com.vl.messenger.ui.utils.compareAsIs
import com.vl.messenger.ui.utils.setOnClick

class NotificationPagingAdapter(
    private val context: Context,
    private val onClick: (Notification) -> Unit
): PagingDataAdapter<Notification, NotificationPagingAdapter.ViewHolder>(compareAsIs()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)!!)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemNotificationBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        ))

    inner class ViewHolder(
        val binding: ItemNotificationBinding
    ): RecyclerView.ViewHolder(binding.root) {

        private lateinit var item: Notification

        init {
            binding.root.setOnClick { onClick(item) }
        }

        fun bind(item: Notification) {
            this.item = item
            with(binding) {
                when (item) {
                    is Notification.Info -> {
                        binding.icon.setImageResource(R.drawable.ic_info)
                        binding.title.text = item.title
                        binding.content.text = item.content
                    }
                    is Notification.FriendRequest -> {
                        binding.icon.setImageResource(R.drawable.ic_person_add)
                        binding.title.text = context.getString(
                            R.string.notification_friend_request_title
                        )
                        binding.content.text = context.getString(
                            R.string.notification_friend_request_content,
                            item.sender.login
                        )
                    }
                    is Notification.InviteToConversation -> {
                        binding.icon.setImageResource(R.drawable.ic_group)
                        binding.title.text = context.getString(
                            R.string.notification_conversation_invite_title
                        )
                        binding.content.text = context.getString(
                            R.string.notification_conversation_invite_content,
                            item.sender.login,
                            item.dialog.title
                        )
                    }
                }
            }
        }
    }
}