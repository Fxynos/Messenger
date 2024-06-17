package com.vl.messenger.data.component

interface ListAdapter<T> {
    val onItemClickListener: OnItemClickListener<T>?
}