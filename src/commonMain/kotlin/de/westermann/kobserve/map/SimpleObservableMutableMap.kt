package de.westermann.kobserve.map

import de.westermann.kobserve.base.ObservableMutableCollection
import de.westermann.kobserve.base.ObservableMutableMap
import de.westermann.kobserve.base.ObservableMutableSet


class SimpleObservableMutableMap<K, V>(
        backingField: MutableMap<K, V>
) : BaseObservableMap<K, V>(backingField), ObservableMutableMap<K, V> {

    private val entrySet = ObservableMutableEntrySet(this, backingField)

    override val entries: ObservableMutableSet<MutableMap.MutableEntry<K, V>> = entrySet
    override val keys: ObservableMutableSet<K> = entrySet.keySet
    override val values: ObservableMutableCollection<V> = entrySet.valueCollection

    override fun put(key: K, value: V): V? {
        val oldElement = backingField.put(key, value)
        if (oldElement == null) {
            emitOnAdd(key, value)
        } else if (oldElement != value) {
            emitOnUpdate(key, oldElement, value)
        }
        return oldElement
    }

    override fun putAll(from: Map<out K, V>) {
        for ((key, value) in from) {
            put(key, value)
        }
    }

    override fun remove(key: K): V? {
        val oldElement = backingField.remove(key)
        if (oldElement != null) {
            emitOnRemove(key, oldElement)
        }
        return oldElement
    }

    override fun clear() {
        val elements = backingField.toMap()
        backingField.clear()
        emitOnClear(elements)
    }
}

fun <K, V> mapProperty(map: MutableMap<K, V>): ObservableMutableMap<K, V> = SimpleObservableMutableMap(map)
fun <K, V> MutableMap<K, V>.asObservable(): ObservableMutableMap<K, V> = SimpleObservableMutableMap(this)
fun <K, V> observableMapOf(vararg entries: Pair<K, V>): ObservableMutableMap<K, V> =
        SimpleObservableMutableMap(mutableMapOf(*entries))
