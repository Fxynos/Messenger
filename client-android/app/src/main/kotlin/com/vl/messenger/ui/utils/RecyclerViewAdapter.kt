package com.vl.messenger.ui.utils

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

fun <T: Any> compareAsIs(): DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem === newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
}