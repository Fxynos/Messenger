package com.vl.messenger.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder

object AdapterDelegatesUtils {
    fun <T, V: ViewBinding> AdapterDelegateViewBindingViewHolder<T, V>.catchClick(
        view: View, onClick: (pos: Int, item: T) -> Unit
    ) = view.setOnClickListener {
        bindingAdapterPosition
            .takeUnless { it == RecyclerView.NO_POSITION }
            ?.also { pos -> onClick(pos, item) }
    }
}