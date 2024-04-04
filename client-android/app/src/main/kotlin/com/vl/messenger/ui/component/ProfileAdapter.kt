package com.vl.messenger.ui.component

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.DownloadManager

class ProfileAdapter(
    context: Context,
    private val downloadManager: DownloadManager
): RecyclerView.Adapter<ContactViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    val items: MutableList<User> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactViewHolder(
            inflater.inflate(R.layout.item_user, parent, false),
            downloadManager
        )

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}