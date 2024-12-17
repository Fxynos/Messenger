package com.vl.messenger.ui.modal

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vl.messenger.R

fun Context.dropConfirmationDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes cancel: Int,
    @StringRes confirm: Int,
    onConfirm: () -> Unit
): AlertDialog = MaterialAlertDialogBuilder(ContextThemeWrapper(this, R.style.AlertDialogStyle))
    .setTitle(title)
    .setMessage(message)
    .setNegativeButton(cancel, null)
    .setPositiveButton(confirm) { _, _ -> onConfirm() }
    .show()