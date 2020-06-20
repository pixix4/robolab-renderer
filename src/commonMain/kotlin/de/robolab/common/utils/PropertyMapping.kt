package de.robolab.common.utils

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <R, T, V> ReadOnlyProperty<R, T>.map(transform: (T) -> V): ReadOnlyProperty<R, V> =
    object : ReadOnlyProperty<R, V> {
        override operator fun getValue(thisRef: R, property: KProperty<*>): V {
            return transform(this@map.getValue(thisRef, property))
        }
    }


fun <R, T, V> ReadWriteProperty<R, T>.map(readTransform: (T) -> V, writeTransform: (V) -> T): ReadWriteProperty<R, V> =
    object : ReadWriteProperty<R, V> {
        override operator fun getValue(thisRef: R, property: KProperty<*>): V {
            return readTransform(this@map.getValue(thisRef, property))
        }

        override operator fun setValue(thisRef: R, property: KProperty<*>, value: V) {
            this@map.setValue(thisRef, property, writeTransform(value))
        }
    }