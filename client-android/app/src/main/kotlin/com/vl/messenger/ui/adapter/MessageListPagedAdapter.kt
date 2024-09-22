package com.vl.messenger.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.vl.messenger.databinding.ItemSignedMessageReceivedBinding
import com.vl.messenger.databinding.ItemUnsignedMessageReceivedBinding
import com.vl.messenger.databinding.ItemUnsignedMessageSentBinding
import com.vl.messenger.ui.adapter.AdapterDelegatesUtils.catchClick
import com.vl.messenger.ui.entity.MessageUi

private typealias OnClickListener = (Int, MessageUi) -> Unit

class MessageListPagedAdapter: BasePagedAdapter<MessageUi>(MessageListPagedAdapter) {
    companion object: DiffUtil.ItemCallback<MessageUi>() {
        override fun areItemsTheSame(oldItem: MessageUi, newItem: MessageUi) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MessageUi, newItem: MessageUi) =
            oldItem == newItem
    }

    init {
        fun onClick(pos: Int, item: MessageUi) { onClickListener?.onClick(pos, item) }
        addDelegates(
            sentMessageDelegate(::onClick),
            receivedUnsignedMessageDelegate(::onClick),
            receivedSignedMessageDelegate(::onClick)
        )
    }
}

private fun sentMessageDelegate(onClick: OnClickListener) = adapterDelegateViewBinding<
        MessageUi.Sent,
        MessageUi,
        ItemUnsignedMessageSentBinding
>(viewBinding = { inflater, parent ->
    ItemUnsignedMessageSentBinding.inflate(inflater, parent, false)
}) {
    catchClick(binding.messageBody, onClick)
    bind {
        binding.content.text = item.message
        binding.time.text = item.time
    }
}

private fun receivedUnsignedMessageDelegate(onClick: OnClickListener) = adapterDelegateViewBinding<
        MessageUi.Received.Unsigned,
        MessageUi,
        ItemUnsignedMessageReceivedBinding
        >(viewBinding = { inflater, parent ->
    ItemUnsignedMessageReceivedBinding.inflate(inflater, parent, false)
}) {
    catchClick(binding.messageBody, onClick)
    bind {
        binding.content.text = item.message
        binding.time.text = item.time
    }
}

private fun receivedSignedMessageDelegate(onClick: OnClickListener) = adapterDelegateViewBinding<
        MessageUi.Received.Signed,
        MessageUi,
        ItemSignedMessageReceivedBinding
        >(viewBinding = { inflater, parent ->
    ItemSignedMessageReceivedBinding.inflate(inflater, parent, false)
}) {
    catchClick(binding.messageBody, onClick)
    bind {
        binding.content.text = item.message
        binding.time.text = item.time
        binding.name.text = item.name
        binding.image.load(item.imageUrl)
    }
}