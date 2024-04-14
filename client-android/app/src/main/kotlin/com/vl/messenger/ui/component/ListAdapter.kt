package com.vl.messenger.ui.component

interface ListAdapter<T> {
    val onItemClickListener: OnItemClickListener<T>?
}