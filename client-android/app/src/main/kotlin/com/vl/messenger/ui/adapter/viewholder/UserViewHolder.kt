package com.vl.messenger.ui.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.databinding.ItemUserBinding
import com.vl.messenger.domain.entity.User

class UserViewHolder(
    private val binding: ItemUserBinding,
    private val onClick: (User) -> Unit
): RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    private lateinit var user: User

    init { binding.root.setOnClickListener(this) }

    fun bind(user: User) {
        this.user = user

        with(binding) {
            hint.visibility = View.GONE
            text.visibility = View.GONE
            title.text = user.login
            image.load(user.imageUrl)
        }
    }

    override fun onClick(view: View): Unit = onClick(user)
}