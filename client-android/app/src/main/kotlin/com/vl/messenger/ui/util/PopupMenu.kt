package com.vl.messenger.ui.util

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu

fun View.dropPopupOptions(vararg items: Pair<Int, Runnable>): Unit =
    PopupMenu(context, this).apply {
        items.forEach { (@StringRes title, callback) ->
            menu.add(title).setOnMenuItemClickListener {
                callback.run()
                true
            }
        }
    }.show()