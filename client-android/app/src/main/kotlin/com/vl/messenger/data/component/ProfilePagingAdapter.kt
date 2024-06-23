package com.vl.messenger.data.component

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.R
import com.vl.messenger.data.entity.User
import com.vl.messenger.domain.ListAdapter
import com.vl.messenger.domain.OnItemClickListener

class ProfilePagingAdapter(
    context: Context,
    private val downloadManager: DownloadManager
): PagingDataAdapter<User, ProfileViewHolder>(ProfilePagingAdapter), ListAdapter<User> {

    private val inflater = LayoutInflater.from(context)
    override var onItemClickListener: OnItemClickListener<User>? = null

    companion object: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem.imageUrl == newItem.imageUrl && oldItem.login == newItem.login
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProfileViewHolder(
            this,
            inflater.inflate(R.layout.item_user, parent, false),
            downloadManager
        )

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) =
        holder.bind(getItem(position)!!)
}