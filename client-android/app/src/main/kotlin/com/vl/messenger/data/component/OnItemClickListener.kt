package com.vl.messenger.data.component

interface OnItemClickListener<T> {
    fun onClick(item: T, position: Int)
}