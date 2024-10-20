package com.vl.messenger.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.ui.adapter.UserAdapter
import com.vl.messenger.domain.OnItemClickListener
import com.vl.messenger.domain.entity.User
import com.vl.messenger.ui.viewmodel.FriendsViewModel
import com.vl.messenger.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendsFragment: Fragment() {

    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var menu: ImageButton
    private lateinit var friends: RecyclerView
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        menu = view.findViewById(R.id.menu)
        adapter = UserAdapter(requireContext())
        friends = view.findViewById(R.id.friends)
        friends.adapter = adapter

        // callbacks
        adapter.onItemClickListener = OnItemClickListener { item: User, _ ->
            startActivity(Intent(requireContext(), UserProfileActivity::class.java).apply {
                putExtra(UserProfileViewModel.ARG_KEY_USER_ID, item.id)
            })
        }
        menu.setOnClickListener {
            (requireActivity() as MenuActivity).openDrawer()
        }
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is FriendsViewModel.UiState.Loaded -> {
                        adapter.items.clear()
                        adapter.items.addAll(uiState.friends)
                        adapter.notifyDataSetChanged()
                    }
                    FriendsViewModel.UiState.Loading -> {
                        adapter.items.clear()
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}