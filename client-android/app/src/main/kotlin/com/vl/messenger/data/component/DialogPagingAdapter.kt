package com.vl.messenger.data.component

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.vl.messenger.R
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.domain.ListAdapter
import com.vl.messenger.domain.OnItemClickListener

class DialogPagingAdapter(
    private val context: Context,
    private val ownUserId: Int
): PagingDataAdapter<Dialog, DialogViewHolder>(DialogPagingAdapter), ListAdapter<Dialog> {
    companion object: DiffUtil.ItemCallback<Dialog>() {
        override fun areItemsTheSame(oldItem: Dialog, newItem: Dialog): Boolean =
            oldItem.id == newItem.id && oldItem.isPrivate == newItem.isPrivate

        override fun areContentsTheSame(oldItem: Dialog, newItem: Dialog): Boolean =
            oldItem == newItem
    }

    private val inflater = LayoutInflater.from(context)
    override var onItemClickListener: OnItemClickListener<Dialog>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DialogViewHolder(
            context,
            this,
            inflater.inflate(R.layout.item_user, parent, false),
            ownUserId
        )

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) =
        holder.bind(getItem(position)!!)
}