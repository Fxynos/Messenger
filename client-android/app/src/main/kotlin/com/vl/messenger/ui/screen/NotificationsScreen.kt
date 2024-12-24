package com.vl.messenger.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.vl.messenger.R
import com.vl.messenger.databinding.FragmentNotificationsBinding
import com.vl.messenger.domain.entity.Notification
import com.vl.messenger.ui.adapter.NotificationPagingAdapter
import com.vl.messenger.ui.modal.dropConfirmationDialog
import com.vl.messenger.ui.viewmodel.NotificationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsScreen: Fragment() {

    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var adapter: NotificationPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        adapter = NotificationPagingAdapter(requireContext()) { notification ->
            when (notification) {
                is Notification.Info -> Unit
                is Notification.FriendRequest -> with(requireContext()) {
                    dropConfirmationDialog(
                        title = getString(R.string.notification_friend_request_title),
                        message = getString(
                            R.string.notification_friend_request_content,
                            notification.sender.login
                        ),
                        cancel = getString(R.string.cancel),
                        confirm = getString(R.string.accept),
                        onConfirm = { viewModel.acceptRequest(notification) }
                    )
                }
                is Notification.InviteToConversation -> with(requireContext()) {
                    dropConfirmationDialog(
                        title = getString(R.string.notification_conversation_invite_title),
                        message = getString(
                            R.string.notification_conversation_invite_content,
                            notification.sender.login,
                            notification.dialog.title
                        ),
                        cancel = getString(R.string.cancel),
                        confirm = getString(R.string.accept),
                        onConfirm = { viewModel.acceptInvite(notification) }
                    )
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            launch { // ui state
                viewModel.uiState.collectLatest(adapter::submitData)
            }
            launch { // load state
                adapter.loadStateFlow.collectLatest {
                    if (it.refresh is LoadState.Loading)
                        return@collectLatest

                    with(binding) {
                        hint.isVisible = adapter.itemCount == 0
                        notifications.isVisible = adapter.itemCount != 0
                    }
                }
            }
        }
    }
}