package de.westermann.kobserve.map

import de.westermann.kobserve.base.ObservableCollection
import de.westermann.kobserve.base.ObservableMap
import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit

open class MapEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V> {

    override fun toString(): String {
        return "$key=$value"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        if (other is Map.Entry<*, *>) {
            return key == other.key && value == other.value
        }

        return false
    }

    override fun hashCode(): Int {
        return (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
    }
}

class ObservableEntrySet<K, V>(
    observableMap: ObservableMap<K, V>,
    private val backingField: Map<K, V>
) : ObservableSet<Map.Entry<K, V>> {

    override val onAdd = EventHandler<Map.Entry<K, V>>()
    override val onRemove = EventHandler<Map.Entry<K, V>>()
    override val onClear = EventHandler<Collection<Map.Entry<K, V>>>()

    override val onChange = EventHandler<Unit>()

    private fun emitOnAdd(element: Map.Entry<K, V>) {
        onAdd.emit(element)
        onChange.emit()
    }

    private fun emitOnRemove(element: Map.Entry<K, V>) {
        onRemove.emit(element)
        onChange.emit()
    }

    private fun emitOnClear(elements: Set<Map.Entry<K, V>>) {
        onClear.emit(elements)
        onChange.emit()
    }

    override val size: Int
        get() = backingField.entries.size

    override fun contains(element: Map.Entry<K, V>): Boolean {
        return backingField[element.key] == element.value
    }

    override fun containsAll(elements: Collection<Map.Entry<K, V>>): Boolean {
        return elements.all(this::contains)
    }

    override fun isEmpty(): Boolean {
        return backingField.entries.isEmpty()
    }

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        return backingField.entries.iterator()
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

    val keySet = ObservableKeySet(observableMap, backingField)
    val valueCollection = ObservableValueCollection(observableMap, backingField)

    init {
        observableMap.onAdd { (key, value) ->
            emitOnAdd(MapEntry(key, value))
        }
        observableMap.onUpdate { (key, oldValue, newValue) ->
            emitOnRemove(MapEntry(key, oldValue))
            emitOnAdd(MapEntry(key, newValue))
        }
        observableMap.onRemove { (key, value) ->
            emitOnRemove(MapEntry(key, value))
        }
        observableMap.onClear {
            emitOnClear(it.entries)
        }
    }
}

open class ObservableKeySet<K, V>(
    observableMap: ObservableMap<K, V>,
    private val backingField: Map<K, V>
) : ObservableSet<K> {

    override val onAdd = EventHandler<K>()
    override val onRemove = EventHandler<K>()
    override val onClear = EventHandler<Collection<K>>()

    override val onChange = EventHandler<Unit>()

    private fun emitOnAdd(element: K) {
        onAdd.emit(element)
        onChange.emit()
    }

    private fun emitOnRemove(element: K) {
        onRemove.emit(element)
        onChange.emit()
    }

    private fun emitOnClear(elements: Set<K>) {
        onClear.emit(elements)
        onChange.emit()
    }

    override val size: Int
        get() = backingField.keys.size

    override fun contains(element: K): Boolean {
        return backingField.keys.contains(element)
    }

    override fun containsAll(elements: Collection<K>): Boolean {
        return backingField.keys.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return backingField.keys.isEmpty()
    }

    override fun iterator(): Iterator<K> {
        return backingField.keys.iterator()
    }

    override fun toString(): String {
        return backingField.keys.toString()
    }

    override fun hashCode(): Int {
        return backingField.keys.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return backingField.keys.equals(other)
    }

    init {
        observableMap.onAdd { (key, _) ->
            emitOnAdd(key)
        }
        observableMap.onRemove { (key, _) ->
            emitOnRemove(key)
        }
        observableMap.onClear {
            emitOnClear(it.keys)
        }
    }
}

open class ObservableValueCollection<K, V>(
    observableMap: ObservableMap<K, V>,
    private val backingField: Map<K, V>
) : ObservableCollection<V> {

    override val onAdd = EventHandler<V>()
    override val onRemove = EventHandler<V>()
    override val onClear = EventHandler<Collection<V>>()

    override val onChange = EventHandler<Unit>()

    private fun emitOnAdd(element: V) {
        onAdd.emit(element)
        onChange.emit()
    }

    private fun emitOnRemove(element: V) {
        onRemove.emit(element)
        onChange.emit()
    }

    private fun emitOnClear(elements: Collection<V>) {
        onClear.emit(elements)
        onChange.emit()
    }

    override val size: Int
        get() = backingField.values.size

    override fun contains(element: V): Boolean {
        return backingField.values.contains(element)
    }

    override fun containsAll(elements: Collection<V>): Boolean {
        return backingField.values.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return backingField.values.isEmpty()
    }

    override fun iterator(): Iterator<V> {
        return backingField.values.iterator()
    }

    override fun toString(): String {
        return backingField.values.toString()
    }

    override fun hashCode(): Int {
        return backingField.values.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return backingField.values.equals(other)
    }

    init {
        observableMap.onAdd { (_, value) ->
            emitOnAdd(value)
        }
        observableMap.onUpdate { (_, oldValue, newValue) ->
            emitOnRemove(oldValue)
            emitOnAdd(newValue)
        }
        observableMap.onRemove { (_, value) ->
            emitOnRemove(value)
        }
        observableMap.onClear {
            emitOnClear(it.values)
        }
    }
}
