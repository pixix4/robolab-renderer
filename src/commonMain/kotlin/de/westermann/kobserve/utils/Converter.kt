package de.westermann.kobserve.utils

import de.westermann.kobserve.base.ObservableCollection
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMap
import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.map.observableMapOf
import de.westermann.kobserve.set.mapObservable
import de.westermann.kobserve.set.observableSetOf

fun <T> ObservableCollection<T>.toObservableList(): ObservableList<T> {
    val list = observableListOf<T>()

    list.addAll(this)

    onAdd {
        list.add(it)
    }
    onRemove {
        list.remove(it)
    }
    onClear {
        list.clear()
    }

    return list
}

fun <T> ObservableCollection<T>.toObservableSet(): ObservableSet<T> {
    val set = observableSetOf<T>()

    set.addAll(this)

    onAdd {
        set.add(it)
    }
    onRemove {
        set.remove(it)
    }
    onClear {
        set.clear()
    }

    return set
}

fun <K, V> ObservableCollection<Pair<K, V>>.toObservableMap(): ObservableMap<K, V> {
    val map = observableMapOf<K, V>()

    map.putAll(this)

    onAdd { (key, value) ->
        map[key] = value
    }
    onRemove { (key, _) ->
        map.remove(key)
    }
    onClear {
        map.clear()
    }

    return map
}

fun <K, V> ObservableMap<K, V>.toObservableSet(): ObservableSet<Pair<K, V>> {
    return entries.mapObservable { it.toPair() }
}

fun <K, V> ObservableMap<K, V>.toObservableList(): ObservableList<Pair<K, V>> {
    return entries.mapObservable { it.toPair() }.toObservableList()
}
