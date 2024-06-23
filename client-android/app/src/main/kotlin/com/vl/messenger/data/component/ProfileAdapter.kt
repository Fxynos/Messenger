package com.vl.messenger.data.component

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.domain.ListAdapter
import com.vl.messenger.domain.OnItemClickListener

class ProfileAdapter(
    context: Context,
    private val downloadManager: DownloadManager
): RecyclerView.Adapter<ProfileViewHolder>(), ListAdapter<User> {
    private val inflater = LayoutInflater.from(context)
    override var onItemClickListener: OnItemClickListener<User>? = null
    val items: MutableList<User> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProfileViewHolder(
            this,
            inflater.inflate(R.layout.item_user, parent, false),
            downloadManager
        )

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}