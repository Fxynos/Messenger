package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vl.messenger.databinding.FragmentDialogsBinding
import com.vl.messenger.ui.adapter.DialogPagingAdapter
import com.vl.messenger.ui.viewmodel.DialogViewModel
import com.vl.messenger.ui.viewmodel.DialogsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogsFragment: Fragment() {

    private val viewModel: DialogsViewModel by viewModels()
    private lateinit var binding: FragmentDialogsBinding
    private lateinit var adapter: DialogPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogsBinding.inflate(inflater, container, false)
        binding.menu.setOnClickListener { (requireActivity() as MenuActivity).openDrawer() }
        adapter = DialogPagingAdapter(requireContext()) { dialog -> // on item click
            startActivity(Intent(requireContext(), DialogActivity::class.java).apply {
                putExtra(DialogViewModel.ARG_DIALOG_ID, dialog.id)
            })
        }
        binding.dialogs.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest(adapter::submitData)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.refresh()
    }
}