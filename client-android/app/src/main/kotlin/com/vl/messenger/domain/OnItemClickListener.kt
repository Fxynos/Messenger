package com.vl.messenger.domain

interface OnItemClickListener<T> {
    fun onClick(item: T, position: Int)
}