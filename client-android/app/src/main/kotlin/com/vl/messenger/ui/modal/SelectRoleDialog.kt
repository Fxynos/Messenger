package com.vl.messenger.ui.modal

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vl.messenger.R
import com.vl.messenger.databinding.DialogSelectRoleBinding
import com.vl.messenger.databinding.ItemRoleBinding
import com.vl.messenger.domain.entity.Role
import com.vl.messenger.domain.entity.User
import com.vl.messenger.ui.utils.compareAsIs

fun Context.dropSelectRoleDialog(
    user: User,
    items: List<Role>,
    onCancel: () -> Unit = {},
    onSelect: (Role) -> Unit
): AlertDialog {
    val contextThemed = ContextThemeWrapper(this, R.style.AlertDialogStyle)
    val binding = DialogSelectRoleBinding.inflate(LayoutInflater.from(contextThemed))
    val dialog = MaterialAlertDialogBuilder(contextThemed)
        .setView(binding.root)
        .setOnCancelListener { onCancel() }
        .show()

    with(binding.user) {
        image.load(user.imageUrl)
        title.text = user.login
    }

    binding.roles.adapter = RoleAdapter(this) {
        onSelect(it)
        dialog.dismiss()
    }.apply {
        submitList(items)
    }

    return dialog
}

private class RoleAdapter(
    context: Context,
    private val onClick: (Role) -> Unit
): ListAdapter<Role, RoleAdapter.ViewHolder>(compareAsIs()) {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemRoleBinding.inflate(inflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)!!)

    inner class ViewHolder(
        private val binding: ItemRoleBinding
    ): RecyclerView.ViewHolder(binding.root) {

        private var item: Role? = null

        init {
            binding.root.setOnClickListener {
                item?.let(onClick)
            }
        }

        fun bind(role: Role) {
            item = role
            binding.title.text = role.name
        }
    }
}