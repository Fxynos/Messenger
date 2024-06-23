package com.vl.messenger.data.component

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.domain.ListAdapter
import com.vl.messenger.domain.OnItemClickListener

class DialogAdapter(
    context: Context,
    private val downloadManager: DownloadManager,
    private val ownUserId: Int
): RecyclerView.Adapter<DialogViewHolder>(), ListAdapter<User> {
    private val inflater = LayoutInflater.from(context)
    override var onItemClickListener: OnItemClickListener<User>? = null
    val items: MutableList<Pair<User, Message>> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DialogViewHolder(
            this,
            inflater.inflate(R.layout.item_user, parent, false),
            downloadManager,
            ownUserId
        )

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item.first, item.second)
    }

    override fun getItemCount() = items.size
}