package com.vl.messenger.ui.component

interface OnItemClickListener<T> {
    fun onClick(item: T, position: Int)
}