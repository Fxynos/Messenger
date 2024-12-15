package com.vl.messenger.ui.modal

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.dropConfirmationDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes cancel: Int,
    @StringRes confirm: Int,
    onConfirm: () -> Unit
): AlertDialog = MaterialAlertDialogBuilder(this)
    .setTitle(title)
    .setMessage(message)
    .setNegativeButton(cancel, null)
    .setPositiveButton(confirm) { _, _ -> onConfirm() }
    .show()