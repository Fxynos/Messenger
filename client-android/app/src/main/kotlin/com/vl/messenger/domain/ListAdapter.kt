package com.vl.messenger.domain

interface ListAdapter<T> {
    val onItemClickListener: OnItemClickListener<T>?
}