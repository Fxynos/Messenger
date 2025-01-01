package com.vl.messenger.ui.modal

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vl.messenger.R

fun Context.dropConfirmationDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes cancel: Int,
    @StringRes confirm: Int,
    onConfirm: () -> Unit
): AlertDialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogStyle)
    .setTitle(title)
    .setMessage(message)
    .setNegativeButton(cancel, null)
    .setPositiveButton(confirm) { _, _ -> onConfirm() }
    .show()

fun Context.dropConfirmationDialog(
    title: String,
    message: String,
    cancel: String,
    confirm: String,
    onConfirm: () -> Unit
): AlertDialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogStyle)
    .setTitle(title)
    .setMessage(message)
    .setNegativeButton(cancel, null)
    .setPositiveButton(confirm) { _, _ -> onConfirm() }
    .show()