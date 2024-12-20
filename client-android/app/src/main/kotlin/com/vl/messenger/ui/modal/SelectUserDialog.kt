package com.vl.messenger.ui.modal

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vl.messenger.R
import com.vl.messenger.databinding.DialogSelectUserBinding
import com.vl.messenger.domain.entity.User
import com.vl.messenger.ui.adapter.UserAdapter

fun Context.dropSelectUserDialog(
    @StringRes title: Int,
    items: List<User>,
    onCancel: () -> Unit = {},
    onSelect: (User) -> Unit
): AlertDialog {
    val contextThemed = ContextThemeWrapper(this, R.style.AlertDialogStyle)
    val binding = DialogSelectUserBinding.inflate(LayoutInflater.from(contextThemed))
    val dialog = MaterialAlertDialogBuilder(contextThemed)
        .setView(binding.root)
        .setOnCancelListener { onCancel() }
        .show()

    binding.title.setText(title)
    binding.options.adapter = UserAdapter(this) {
        onSelect(it)
        dialog.dismiss()
    }.apply {
        submitList(items)
    }

    return dialog
}