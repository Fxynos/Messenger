package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vl.messenger.databinding.FragmentSearchBinding
import com.vl.messenger.ui.adapter.UserPagingAdapter
import com.vl.messenger.ui.viewmodel.SearchViewModel
import com.vl.messenger.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment: Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: UserPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // callbacks
        adapter = UserPagingAdapter(requireContext()) { user -> // on item click
            startActivity(Intent(requireContext(), UserProfileActivity::class.java).apply {
                putExtra(UserProfileViewModel.ARG_KEY_USER_ID, user.id)
            })
        }
        with(binding) {
            menu.setOnClickListener { (requireActivity() as MenuActivity).openDrawer() }
            search.setOnClickListener { viewModel.search(input.text.toString().trim()) }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // subscriptions
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                    with(binding) {
                        hint.isVisible = adapter.itemCount == 0
                        result.isVisible = adapter.itemCount != 0
                        result.scrollToPosition(0)
                    }
                }
            }
        }
    }
}