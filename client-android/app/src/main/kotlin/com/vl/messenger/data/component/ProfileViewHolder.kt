package com.vl.messenger.data.component

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.R
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewHolder(
    private val adapter: ListAdapter<User>,
    view: View,
    private val downloadManager: DownloadManager
): RecyclerView.ViewHolder(view), View.OnClickListener {

    private val title: TextView = view.findViewById(R.id.title)
    private val hint: TextView = view.findViewById(R.id.hint)
    private val text: TextView = view.findViewById(R.id.text)
    private val image: ImageView = view.findViewById(R.id.image)
    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var user: User

    init { view.setOnClickListener(this) }

    fun bind(user: User) {
        this.user = user

        hint.visibility = View.GONE
        text.visibility = View.GONE
        title.text = user.login

        scope.coroutineContext.cancelChildren()

        if (user.imageUrl == null)
            image.setImageBitmap(null)
        else scope.launch {
            val bitmap = withContext(Dispatchers.IO) { downloadManager.downloadBitmap(user.imageUrl) }
            withContext(Dispatchers.Main) { image.setImageBitmap(bitmap) }
        }
    }

    override fun onClick(view: View) {
        adapter.onItemClickListener?.onClick(user, absoluteAdapterPosition)
    }
}