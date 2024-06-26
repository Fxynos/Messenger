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
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.domain.OnItemClickListener
import com.vl.messenger.data.component.ProfileAdapter
import com.vl.messenger.ui.viewmodel.FriendsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects
import javax.inject.Inject

@AndroidEntryPoint
class FriendsFragment: Fragment(), View.OnClickListener, OnItemClickListener<User> {

    @Inject
    lateinit var downloadManager: DownloadManager
    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var menu: ImageButton
    private lateinit var friends: RecyclerView
    private lateinit var adapter: ProfileAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        menu = view.findViewById(R.id.menu)
        menu.setOnClickListener(this)
        adapter = ProfileAdapter(requireContext(), downloadManager)
        adapter.onItemClickListener = this
        friends = view.findViewById(R.id.friends)
        friends.adapter = adapter
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.friends.collect { items ->
                if (items == null) return@collect
                adapter.items.removeAll(Objects::nonNull) // removes absolutely all
                adapter.items.addAll(items)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchFriends()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.menu -> (requireActivity() as MenuActivity).openDrawer()
        }
    }

    override fun onClick(item: User, position: Int) {
        startActivity(Intent(requireContext(), UserProfileActivity::class.java).apply {
            putExtra("user", item)
        })
    }
}