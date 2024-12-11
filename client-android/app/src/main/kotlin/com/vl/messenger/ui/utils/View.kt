package com.vl.messenger.ui.utils

import android.view.View

inline fun View.setOnClick(crossinline block: () -> Unit): Unit = setOnClickListener { block() }