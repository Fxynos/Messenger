package com.vl.messenger.data.component

import com.vl.messenger.domain.Dao
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CacheDao<K, V>: Dao<K, V> {
    private val _updateEvents = MutableSharedFlow<Any>(
        onBufferOverflow = BufferOverflow.DROP_LATEST,
        extraBufferCapacity = 1
    )
    val updateEvents = _updateEvents.asSharedFlow()

    private val cache: LinkedHashMap<K, V> = LinkedHashMap()

    override val first: V? get() = cache.asSequence().firstOrNull()?.value
    override val last: V? get() = cache.asSequence().lastOrNull()?.value

    override fun get(key: K): V = cache[key] ?: throw NoSuchElementException(key.toString())

    override fun set(key: K, value: V) {
        cache[key] = value
        produceUpdateEvent()
    }

    override fun getPage(key: K?, size: Int): List<V> =
        cache.asSequence()
            .run {
                if (key != null)
                    dropWhile { (itemKey, _) ->
                        itemKey != key
                    }.drop(1)
                else this
            }.take(size)
            .map(Map.Entry<K, V>::value)
            .toList()

    override fun addLast(items: List<Pair<K, V>>) {
        cache.putAll(items)
        produceUpdateEvent()
    }

    override fun addFirst(items: List<Pair<K, V>>) {
        @Suppress("unchecked")
        val tail = cache.clone() as Map<K, V>
        cache.clear()
        cache.putAll(items)
        cache.putAll(tail)
        produceUpdateEvent()
    }

    private fun produceUpdateEvent() = _updateEvents.tryEmit(Any())
}