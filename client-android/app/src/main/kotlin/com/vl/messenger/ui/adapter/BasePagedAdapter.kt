package com.vl.messenger.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.paging.PagedListDelegationAdapter

abstract class BasePagedAdapter<T>(
    diffUtil: DiffUtil.ItemCallback<T>
): PagedListDelegationAdapter<T>(diffUtil) {

    protected var onClickListener: OnItemClickListener<T>? = null
        private set

    protected fun addDelegates(vararg list: AdapterDelegate<List<T>>) = list.forEach {
        delegatesManager.addDelegate(it)
    }

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        this.onClickListener = listener
    }

    fun interface OnItemClickListener<T> {
        fun onClick(position: Int, item: T)
    }
}