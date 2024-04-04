package com.vl.messenger.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.ui.component.ProfilePagingAdapter
import com.vl.messenger.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(), View.OnClickListener {

    @Inject lateinit var downloadManager: DownloadManager
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var menu: ImageButton
    private lateinit var search: ImageButton
    private lateinit var input: EditText
    private lateinit var result: RecyclerView
    private lateinit var hint: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false).apply {
        menu = findViewById(R.id.menu)
        search = findViewById(R.id.search)
        input = findViewById(R.id.input)
        result = findViewById(R.id.result)
        hint = findViewById(R.id.hint)
        menu.setOnClickListener(this@SearchFragment)
        search.setOnClickListener(this@SearchFragment)
        input.doOnTextChanged { pattern, _, _, _ ->
             pattern?.toString()
                 ?.trim()
                 ?.takeIf(String::isNotEmpty)
                 ?.let(this@SearchFragment::searchUsers)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.menu -> (requireActivity() as MenuActivity).openDrawer()
            R.id.search -> input.text.toString()
                .trim()
                .takeIf(String::isNotEmpty)
                ?.let(this::searchUsers)
        }
    }

    private fun searchUsers(pattern: String) {
        val adapter = ProfilePagingAdapter(requireContext(), downloadManager)
        hint.visibility = View.GONE
        result.visibility = View.VISIBLE // TODO check if there are results
        result.scrollToPosition(0)
        result.adapter = adapter
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel
                .search(pattern)
                .collectLatest(adapter::submitData)
        }
    }
}