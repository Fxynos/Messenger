package com.vl.messenger.ui.adapter.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vl.messenger.R
import com.vl.messenger.domain.ListAdapter
import com.vl.messenger.domain.entity.User

class UserViewHolder(
    view: View, // TODO use view binding
    private val onClick: (User) -> Unit
): RecyclerView.ViewHolder(view), View.OnClickListener {

    private val title: TextView = view.findViewById(R.id.title)
    private val hint: TextView = view.findViewById(R.id.hint)
    private val text: TextView = view.findViewById(R.id.text)
    private val image: ImageView = view.findViewById(R.id.image)

    private lateinit var user: User

    init { view.setOnClickListener(this) }

    fun bind(user: User) {
        this.user = user

        hint.visibility = View.GONE
        text.visibility = View.GONE
        title.text = user.login
        image.load(user.imageUrl)
    }

    override fun onClick(view: View) {
        onClick(user)
    }
}