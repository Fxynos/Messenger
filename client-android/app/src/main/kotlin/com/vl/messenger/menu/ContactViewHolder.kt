package com.vl.messenger.menu

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.menu.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewHolder(
    view: View,
    private val downloadManager: DownloadManager
): RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.title)
    private val hint: TextView = view.findViewById(R.id.hint)
    private val text: TextView = view.findViewById(R.id.text)
    private val image: ImageView = view.findViewById(R.id.image)
    private val scope = CoroutineScope(Dispatchers.Default)

    fun bind(user: User) {
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
}