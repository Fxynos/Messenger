package com.vl.messenger.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.vl.messenger.databinding.ItemUserBinding
import com.vl.messenger.domain.entity.User
import com.vl.messenger.ui.adapter.viewholder.UserViewHolder

class UserAdapter(
    context: Context,
    private val onClick: (User) -> Unit
): ListAdapter<User, UserViewHolder>(UserAdapter) {
    companion object: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserViewHolder(
            ItemUserBinding.inflate(inflater, parent, false),
            onClick = onClick
        )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(getItem(position))
}