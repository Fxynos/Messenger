package com.vl.messenger.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.vl.messenger.databinding.ItemUserBinding
import com.vl.messenger.domain.entity.User
import com.vl.messenger.ui.adapter.viewholder.UserViewHolder

class UserPagingAdapter(
    context: Context,
    private val onClick: (User) -> Unit
): PagingDataAdapter<User, UserViewHolder>(UserPagingAdapter) {
    companion object: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }

    private val inflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(getItem(position)!!)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder = UserViewHolder(
        ItemUserBinding.inflate(inflater, parent, false),
        onClick = onClick
    )
}