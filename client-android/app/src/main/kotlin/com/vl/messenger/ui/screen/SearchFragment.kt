package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
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
            result.adapter = adapter
            menu.setOnClickListener { (requireActivity() as MenuActivity).openDrawer() }

            // search on button click, IME action or input
            search.setOnClickListener { search() }
            input.setOnEditorActionListener { _, code, _ ->
                if (code == EditorInfo.IME_ACTION_SEARCH)
                    search()

                false
            }
            input.addTextChangedListener(afterTextChanged = { search() })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // subscriptions
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { // items
                    viewModel.uiState.collectLatest(adapter::submitData)
                }
                launch { // load state
                    adapter.loadStateFlow.collectLatest {
                        if (it.refresh is LoadState.Loading)
                            return@collectLatest

                        with(binding) {
                            hint.isVisible = adapter.itemCount == 0
                            result.isVisible = adapter.itemCount != 0
                        }
                    }
                }
            }
        }
    }

    private fun search() {
        viewModel.search(binding.input.text.toString().trim())
    }
}