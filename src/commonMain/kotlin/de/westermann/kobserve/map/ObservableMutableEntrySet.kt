package de.westermann.kobserve.map

import de.westermann.kobserve.base.ObservableMutableCollection
import de.westermann.kobserve.base.ObservableMutableMap
import de.westermann.kobserve.base.ObservableMutableSet
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.utils.ObservableMutableIterator

class MutableMapEntry<K, V>(
    override val key: K,
    override var value: V,
    private val onSet: (K, V) -> V?
) : MapEntry<K, V>(key, value), MutableMap.MutableEntry<K, V> {

    override fun setValue(newValue: V): V {
        value = newValue
        return onSet(key, newValue) ?: throw UnsupportedOperationException()
    }
}

class ObservableMutableEntrySet<K, V>(
    private val observableMap: ObservableMutableMap<K, V>,
    private val backingField: MutableMap<K, V>
) : ObservableMutableSet<MutableMap.MutableEntry<K, V>> {

    override val onAdd = EventHandler<MutableMap.MutableEntry<K, V>>()
    override val onRemove = EventHandler<MutableMap.MutableEntry<K, V>>()
    override val onClear = EventHandler<Collection<MutableMap.MutableEntry<K, V>>>()

    override val onChange = EventHandler<Unit>()

    private fun emitOnAdd(element: MutableMap.MutableEntry<K, V>) {
        onAdd.emit(element)
        onChange.emit()
    }

    private fun emitOnRemove(element: MutableMap.MutableEntry<K, V>) {
        onRemove.emit(element)
        onChange.emit()
    }

    private fun emitOnClear(elements: Set<MutableMap.MutableEntry<K, V>>) {
        onClear.emit(elements)
        onChange.emit()
    }

    override val size: Int
        get() = backingField.entries.size

    override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
        return backingField[element.key] == element.value
    }

    override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        return elements.all(this::contains)
    }

    override fun isEmpty(): Boolean {
        return backingField.entries.isEmpty()
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
        return MutableEntrySetIterator()
    }

    override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        observableMap.clear()
    }

    override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
        return observableMap.remove(element.key) != null
    }

    override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        var anyRemoved = false

        for (element in elements) {
            if (observableMap.remove(element.key) != null) {
                anyRemoved = true
            }
        }

        return anyRemoved
    }

    override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        var anyRemoved = false

        val retainKeys = elements.map { it.key }.toSet()
        for (element in backingField.keys.toList()) {
            if (element in retainKeys) continue

            if (observableMap.remove(element) != null) {
                anyRemoved = true
            }
        }

        return anyRemoved
    }

    override fun toString(): String {
        return backingField.entries.toString()
    }

    override fun hashCode(): Int {
        return backingField.entries.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return backingField.entries.equals(other)
    }

    val keySet = ObservableMutableKeySet(observableMap, backingField)
    val valueCollection = ObservableMutableValueCollection(observableMap, backingField)

    init {
        observableMap.onAdd { (key, value) ->
            emitOnAdd(MutableMapEntry(key, value, observableMap::put))
        }
        observableMap.onUpdate { (key, oldValue, newValue) ->
            onRemove.emit(MutableMapEntry(key, oldValue, observableMap::put))
            onAdd.emit(MutableMapEntry(key, newValue, observableMap::put))
            onChange.emit()
        }
        observableMap.onRemove { (key, value) ->
            emitOnRemove(MutableMapEntry(key, value, observableMap::put))
        }
        observableMap.onClear {
            emitOnClear(it.entries.map { (key, value) -> MutableMapEntry(key, value, observableMap::put) }.toSet())
        }
    }

    inner class MutableEntrySetIterator() : MutableIterator<MutableMap.MutableEntry<K, V>> {

        private val iterator = backingField.entries.iterator()

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        private var lastElement: K? = null

        override fun next(): MutableMap.MutableEntry<K, V> {
            val element = iterator.next()

            lastElement = element.key

            return MutableMapEntry(element.key, element.value, observableMap::put)
        }

        override fun remove() {
            iterator.remove()

            @Suppress("UNCHECKED_CAST")
            observableMap.remove(lastElement as K)
        }

    }
}

class ObservableMutableKeySet<K, V>(
    private val observableMap: ObservableMutableMap<K, V>,
    private val backingField: MutableMap<K, V>
) : ObservableKeySet<K, V>(observableMap, backingField), ObservableMutableSet<K> {

    override fun iterator(): MutableIterator<K> {
        return ObservableMutableIterator(backingField.keys.iterator()) {
            remove(it)
        }
    }

    override fun add(element: K): Boolean {
        throw UnsupportedOperationException()
    }

    override fun addAll(elements: Collection<K>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        observableMap.clear()
    }

    override fun remove(element: K): Boolean {
        return observableMap.remove(element) != null
    }

    override fun removeAll(elements: Collection<K>): Boolean {
        var anyRemoved = false

        for (element in elements) {
            if (observableMap.remove(element) != null) {
                anyRemoved = true
            }
        }

        return anyRemoved
    }

    override fun retainAll(elements: Collection<K>): Boolean {
        var anyRemoved = false

        for (element in backingField.keys.toList()) {
            if (element in elements) continue

            if (observableMap.remove(element) != null) {
                anyRemoved = true
            }
        }

        return anyRemoved
    }
}

class ObservableMutableValueCollection<K, V>(
    private val observableMap: ObservableMutableMap<K, V>,
    private val backingField: MutableMap<K, V>
) : ObservableValueCollection<K, V>(observableMap, backingField), ObservableMutableCollection<V> {

    override fun iterator(): MutableIterator<V> {
        return ObservableMutableIterator(backingField.values.iterator()) {
            remove(it)
        }
    }

    override fun add(element: V): Boolean {
        throw UnsupportedOperationException()
    }

    override fun addAll(elements: Collection<V>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        observableMap.clear()
    }

    override fun remove(element: V): Boolean {
        val keysToRemove = observableMap.filter { it.value == element }.keys.take(1)

        for (key in keysToRemove) {
            observableMap.remove(key)
        }

        return keysToRemove.isNotEmpty()
    }

    override fun removeAll(elements: Collection<V>): Boolean {
        val keysToRemove = observableMap.filter { it.value in elements }.keys

        for (key in keysToRemove) {
            observableMap.remove(key)
        }

        return keysToRemove.isNotEmpty()
    }

    override fun retainAll(elements: Collection<V>): Boolean {
        val keysToRemove = observableMap.filterNot { it.value in elements }.keys

        for (key in keysToRemove) {
            observableMap.remove(key)
        }

        return keysToRemove.isNotEmpty()
    }
}
