package de.westermann.kobserve.map

import de.westermann.kobserve.base.ObservableCollection
import de.westermann.kobserve.base.ObservableMap
import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit

abstract class BaseObservableMap<K, V>(
        protected val backingField: MutableMap<K, V>
) : ObservableMap<K, V> {

    override val onAdd = EventHandler<ObservableMap.AddEvent<K, V>>()
    override val onUpdate = EventHandler<ObservableMap.UpdateEvent<K, V>>()
    override val onRemove = EventHandler<ObservableMap.RemoveEvent<K, V>>()
    override val onClear = EventHandler<Map<K, V>>()

    override val onChange = EventHandler<Unit>()

    protected fun emitOnAdd(key: K, value: V) {
        onAdd.emit(ObservableMap.AddEvent(key, value))
        onChange.emit()
    }

    protected fun emitOnUpdate(key: K, oldValue: V, newValue: V) {
        onUpdate.emit(ObservableMap.UpdateEvent(key, oldValue, newValue))
        onChange.emit()
    }

    protected fun emitOnRemove(key: K, value: V) {
        onRemove.emit(ObservableMap.RemoveEvent(key, value))
        onChange.emit()
    }

    protected fun emitOnClear(elements: Map<K, V>) {
        onClear.emit(elements)
        onChange.emit()
    }

    override val size: Int
        get() = backingField.size

    override fun containsKey(key: K): Boolean {
        return backingField.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return backingField.containsValue(value)
    }

    override fun isEmpty(): Boolean {
        return backingField.isEmpty()
    }

    private val entrySet = ObservableEntrySet(this, backingField)

    override val entries: ObservableSet<Map.Entry<K, V>> = entrySet
    override val keys: ObservableSet<K> = entrySet.keySet
    override val values: ObservableCollection<V> = entrySet.valueCollection

    override fun get(key: K): V? {
        return backingField[key]
    }

    override fun toString(): String {
        return backingField.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        if (other is BaseObservableMap<*, *>) {
            return backingField == other.backingField
        }

        return backingField == other
    }

    override fun hashCode(): Int {
        return backingField.hashCode()
    }
}
