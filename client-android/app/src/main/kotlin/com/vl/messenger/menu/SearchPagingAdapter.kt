package com.vl.messenger.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.menu.entity.User

class SearchPagingAdapter(
    context: Context
): PagingDataAdapter<User, SearchPagingAdapter.ViewHolder>(SearchPagingAdapter) {

    private val inflater = LayoutInflater.from(context)

    companion object: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem.image == newItem.image && oldItem.login == newItem.login
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(inflater.inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)!!)

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.title)
        private val hint: TextView = view.findViewById(R.id.hint)
        private val text: TextView = view.findViewById(R.id.text)
        private val image: ImageView = view.findViewById(R.id.image)

        fun bind(user: User) {
            hint.visibility = View.GONE
            text.visibility = View.GONE
            title.text = user.login
            // TODO set image
        }
    }
}