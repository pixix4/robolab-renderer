package de.westermann.kobserve.base

import de.westermann.kobserve.event.EventHandler
import kotlin.reflect.KProperty

interface ObservableMap<K, V> : ObservableValue<Map<K, V>>, Map<K, V> {

    val onAdd: EventHandler<AddEvent<K, V>>
    val onUpdate: EventHandler<UpdateEvent<K, V>>
    val onRemove: EventHandler<RemoveEvent<K, V>>
    val onClear: EventHandler<Map<K, V>>

    override val entries: ObservableSet<Map.Entry<K, V>>

    override val keys: ObservableSet<K>

    override val values: ObservableCollection<V>

    override fun get(): Map<K, V> {
        return this
    }

    data class AddEvent<K, V>(
            val key: K,
            val value: V
    )

    data class UpdateEvent<K, V>(
            val key: K,
            val oldValue: V,
            val newElement: V
    )

    data class RemoveEvent<K, V>(
            val key: K,
            val value: V
    )

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?

    override val value: Map<K, V>
        get() = get()

    override fun getValue(container: Any?, property: KProperty<*>): Map<K, V> = get()
    override val onChange: EventHandler<Unit>
    override fun invalidate() {}
}
