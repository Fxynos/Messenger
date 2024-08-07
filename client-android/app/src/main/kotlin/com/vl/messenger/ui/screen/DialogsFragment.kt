package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import com.vl.messenger.data.component.DialogPagingAdapter
import com.vl.messenger.data.entity.Dialog
import com.vl.messenger.domain.OnItemClickListener
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.ui.viewmodel.DialogsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DialogsFragment: Fragment(), View.OnClickListener, OnItemClickListener<Dialog> {

    @Inject lateinit var downloadManager: DownloadManager
    private val viewModel: DialogsViewModel by viewModels()
    private lateinit var menu: ImageButton
    private lateinit var dialogs: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view =  inflater.inflate(R.layout.fragment_dialogs, container, false)
        menu = view.findViewById(R.id.menu)
        menu.setOnClickListener(this)
        dialogs = view.findViewById(R.id.dialogs)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val userId = withContext(Dispatchers.IO) { getOwnUserId() }
            val adapter = DialogPagingAdapter(requireContext(), userId)
            adapter.onItemClickListener = this@DialogsFragment
            dialogs.adapter = adapter

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.refresh()
                viewModel.dialogs.collect(adapter::submitData)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.menu -> (requireActivity() as MenuActivity).openDrawer()
        }
    }

    override fun onClick(item: Dialog, position: Int) {
        startActivity(Intent(requireContext(), DialogActivity::class.java).apply {
            putExtra(DialogActivity.EXTRA_OWN_ID, runBlocking { getOwnUserId() })
            putExtra(DialogActivity.EXTRA_DIALOG, item)
        })
    }

    private suspend fun getOwnUserId() =
        (requireActivity() as MenuActivity).ownUser.filterNotNull().first().id
}