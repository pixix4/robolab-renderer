package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import kotlin.reflect.KProperty

interface ObservableMutableMap<K, V> : ObservableMap<K, V>, MutableMap<K, V> {

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?

    override val onAdd: EventHandler<ObservableMap.AddEvent<K, V>>
    override val onUpdate: EventHandler<ObservableMap.UpdateEvent<K, V>>
    override val onRemove: EventHandler<ObservableMap.RemoveEvent<K, V>>
    override val onClear: EventHandler<Map<K, V>>

    override fun clear()
    override fun put(key: K, value: V): V?
    override fun putAll(from: Map<out K, V>)
    override fun remove(key: K): V?

    override val value: MutableMap<K, V>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): MutableMap<K, V> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
    override fun get(): MutableMap<K, V> {
        return this
    }

    override val entries: ObservableMutableSet<MutableMap.MutableEntry<K, V>>

    override val keys: ObservableMutableSet<K>

    override val values: ObservableMutableCollection<V>
}
