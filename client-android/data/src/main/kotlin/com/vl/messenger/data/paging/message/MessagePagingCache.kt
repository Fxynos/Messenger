package com.vl.messenger.data.paging.message

import com.vl.messenger.domain.boundary.PagingCache
import com.vl.messenger.domain.entity.Message
import java.util.LinkedList

internal class MessagePagingCache: PagingCache<Long, Message> {

    private val cache = LinkedList<Message>()

    override val first: Message? get() = cache.firstOrNull()
    override val last: Message? get() = cache.lastOrNull()

    override fun get(key: Long): Message = cache.first { it.id == key }

    override fun set(key: Long, value: Message) {
        val iterator = cache.listIterator()
        while (iterator.hasNext())
            if (iterator.next().id == key) {
                iterator.set(value)
                return
            }
    }

    override fun getPage(key: Long?, size: Int): List<Message> =
        cache.asSequence()
            .run {
                if (key != null)
                    dropWhile { it.id != key }
                else this
            }.take(size)
            .toList()

    override fun addLast(items: List<Message>) {
        cache.addAll(items)
    }

    override fun addFirst(items: List<Message>) {
        cache.addAll(0, items)
    }
}