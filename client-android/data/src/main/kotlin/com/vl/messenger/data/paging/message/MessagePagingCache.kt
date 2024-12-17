package com.vl.messenger.data.paging.message

import android.util.Log
import com.vl.messenger.domain.boundary.PagingCache
import com.vl.messenger.domain.entity.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val TAG = "MessagePagingCache"

internal class MessagePagingCache: PagingCache<Long, Message> {

    private var cache = LinkedHashSet<Message>()
    private val _updateEvents = MutableSharedFlow<Any>(extraBufferCapacity = 1)

    override val first: Message? get() = cache.firstOrNull()
    override val last: Message? get() = cache.lastOrNull()
    override val updateEvents: SharedFlow<Any> = _updateEvents.asSharedFlow()

    override fun get(key: Long): Message = cache.first { it.id == key }
        .also { Log.d(TAG, "get $it") }

    override fun set(key: Long, value: Message) {
        Log.d(TAG, "set $value")
        cache.add(value)
        produceUpdateEvent()
    }

    override fun getPage(key: Long?, size: Int): List<Message> =
        cache.asSequence()
            .run {
                if (key != null)
                    dropWhile { it.id != key }
                else this
            }.take(size)
            .toList()
            .also {
                Log.d(TAG, "getPage(key=$key, size=$size): ${it.toTypedArray().contentToString()}")
            }

    override fun addLast(items: List<Message>) {
        Log.d(TAG, "addLast(${items.toTypedArray().contentToString()})")
        cache.addAll(items)
        Log.d(TAG, "Cache: $cache")
        produceUpdateEvent()
    }

    override fun addFirst(items: List<Message>) {
        Log.d(TAG, "addFirst(${items.toTypedArray().contentToString()})")
        cache = LinkedHashSet(items.filter { !cache.contains(it) } + cache) // FIXME expensive operation
        Log.d(TAG, "Cache: $cache")
        produceUpdateEvent()
    }

    private fun produceUpdateEvent() {
        val emitted = _updateEvents.tryEmit(Any())
        Log.d(TAG, "produceUpdateEvent(): $emitted")
    }
}