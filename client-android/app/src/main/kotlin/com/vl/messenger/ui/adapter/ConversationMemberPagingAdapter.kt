package com.vl.messenger.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.databinding.ItemUserBinding
import com.vl.messenger.domain.entity.ConversationMember

class ConversationMemberPagingAdapter(
    context: Context,
    private val onClick: (ConversationMember) -> Unit
): PagingDataAdapter<ConversationMember, ConversationMemberPagingAdapter.ViewHolder>(
    ConversationMemberPagingAdapter
) {
    companion object: DiffUtil.ItemCallback<ConversationMember>() {
        override fun areItemsTheSame(
            oldItem: ConversationMember,
            newItem: ConversationMember
        ): Boolean = oldItem === newItem

        override fun areContentsTheSame(
            oldItem: ConversationMember,
            newItem: ConversationMember
        ): Boolean = oldItem == newItem
    }

    private val inflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)!!)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemUserBinding.inflate(inflater), onClick)

    class ViewHolder(
        private val binding: ItemUserBinding,
        onClick: (ConversationMember) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {

        private var member: ConversationMember? = null

        init {
            binding.root.setOnClickListener {
                member?.let { onClick(it) }
            }
        }

        fun bind(member: ConversationMember) {
            this.member = member

            with(binding) {
                text.visibility = View.GONE
                hint.text = member.role.name
                title.text = member.user.login
                image.load(member.user.imageUrl)
            }
        }
    }
}