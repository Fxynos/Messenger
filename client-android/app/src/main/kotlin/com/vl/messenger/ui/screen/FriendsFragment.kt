package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.vl.messenger.databinding.FragmentFriendsBinding
import com.vl.messenger.ui.adapter.UserAdapter
import com.vl.messenger.ui.viewmodel.FriendsViewModel
import com.vl.messenger.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendsFragment: Fragment() {

    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var binding: FragmentFriendsBinding
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)
        adapter = UserAdapter(requireContext()) { clickedUser ->
            startActivity(Intent(requireContext(), UserProfileActivity::class.java).apply {
                putExtra(UserProfileViewModel.ARG_KEY_USER_ID, clickedUser.id)
            })
        }
        with(binding) {
            friends.adapter = adapter
            menu.setOnClickListener {
                (requireActivity() as MenuActivity).openDrawer()
            }
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is FriendsViewModel.UiState.Loaded -> {
                        adapter.submitList(uiState.friends)
                        binding.hint.isVisible = adapter.itemCount == 0
                        binding.friends.isVisible = adapter.itemCount != 0
                    }
                    FriendsViewModel.UiState.Loading -> adapter.submitList(emptyList())
                }
            }
        }
    }
}