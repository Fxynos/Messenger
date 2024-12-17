package com.vl.messenger.ui.modal

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vl.messenger.R
import com.vl.messenger.databinding.DialogInputTextBinding

fun Context.dropTextInputDialog(
    @StringRes headerTitle: Int,
    @StringRes hintTitle: Int,
    @StringRes okButton: Int,
    @StringRes cancelButton: Int,
    onCancel: () -> Unit = {},
    onConfirm: (String) -> Unit
): AlertDialog {
    val contextThemed = ContextThemeWrapper(this, R.style.AlertDialogStyle)
    val binding = DialogInputTextBinding.inflate(LayoutInflater.from(contextThemed))
    val dialog = MaterialAlertDialogBuilder(contextThemed)
        .setView(binding.root)
        .show()

    with(binding) {
        title.setText(headerTitle)
        input.setHint(hintTitle)
        ok.setText(okButton)
        cancel.setText(cancelButton)

        ok.isEnabled = false
        input.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            binding.ok.isEnabled = !text.isNullOrBlank()
        })

        cancel.setOnClickListener {
            onCancel()
            dialog.dismiss()
        }
        ok.setOnClickListener {
            onConfirm(binding.input.text.toString().trim())
            dialog.dismiss()
        }
    }

    return dialog
}