package com.vl.messenger.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.domain.entity.User
import com.vl.messenger.ui.adapter.viewholder.UserViewHolder

class UserAdapter(
    context: Context,
    private val onClick: (User) -> Unit
): RecyclerView.Adapter<UserViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    val items: MutableList<User> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserViewHolder(
            view = inflater.inflate(R.layout.item_user, parent, false),
            onClick = onClick
        )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}