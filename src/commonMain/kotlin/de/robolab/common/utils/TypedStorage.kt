package de.robolab.common.utils

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

open class TypedStorage {

    private val storage = KeyValueStorage()
    private val itemList = mutableListOf<Item<*>>()

    fun clear() {
        for (item in itemList) {
            item.clear()
        }

        storage.clear()
    }

    fun saveDefaults() {
        for (item in itemList) {
            item.save()
        }
    }

    @Suppress("LeakingThis")
    abstract inner class Item<T : Any>(private val key: String, val default: T) : ObservableProperty<T> {
        protected abstract fun serialize(value: T): String?
        protected abstract fun deserialize(value: String): T?

        override val onChange = EventHandler<Unit>()
        override var binding: Binding<T> = Binding.Unbound()

        override fun get(): T {
            val value = storage[key] ?: return default
            return deserialize(value) ?: default
        }

        override fun set(value: T) {
            val newValue = serialize(value)
            if (newValue != storage[key]) {
                storage[key] = newValue
                onChange.emit(Unit)
            }
        }

        fun save() {
            set(get())
        }

        fun clear() {
            val current = get()
            if (current != default) {
                storage[key] = null
                onChange.emit(Unit)
            }
        }

        init {
            itemList += this
        }
    }

    protected fun item(key: String, default: Double): Item<Double> = DoubleItem(key, default)
    private inner class DoubleItem(key: String, default: Double) : Item<Double>(key, default) {
        override fun serialize(value: Double): String? {
            return value.toString()
        }

        override fun deserialize(value: String): Double? {
            return value.toDoubleOrNull()
        }
    }

    internal fun item(key: String, default: Int): Item<Int> = IntItem(key, default)
    private inner class IntItem(key: String, default: Int) : Item<Int>(key, default) {
        override fun serialize(value: Int): String? {
            return value.toString()
        }

        override fun deserialize(value: String): Int? {
            return value.toIntOrNull()
        }
    }

    internal fun item(key: String, default: String): Item<String> = StringItem(key, default)
    private inner class StringItem(key: String, default: String) : Item<String>(key, default) {
        override fun serialize(value: String): String? {
            return value
        }

        override fun deserialize(value: String): String? {
            return value
        }
    }

    internal fun item(key: String, default: Boolean): Item<Boolean> = BooleanItem(key, default)
    private inner class BooleanItem(key: String, default: Boolean) : Item<Boolean>(key, default) {
        override fun serialize(value: Boolean): String? {
            return value.toString()
        }

        override fun deserialize(value: String): Boolean? {
            return value.toLowerCase() == "true"
        }
    }

    internal inline fun <reified T : Enum<T>> item(key: String, default: T): Item<T> =
        item(key, default, enumValues())

    internal fun <T : Enum<T>> item(key: String, default: T, valueList: Array<T>): Item<T> =
        EnumItem(key, default, valueList)

    private inner class EnumItem<T : Enum<T>>(key: String, default: T, private val valueList: Array<T>) :
        Item<T>(key, default) {
        override fun serialize(value: T): String? {
            return value.name
        }

        override fun deserialize(value: String): T? {
            return valueList.find { it.name == value }
        }
    }

    private val json = Json(JsonConfiguration.Stable)
    internal fun item(key: String, default: List<String>): Item<List<String>> = StringListItem(key, default)
    private inner class StringListItem(key: String, default: List<String>) : Item<List<String>>(key, default) {
        override fun serialize(value: List<String>): String? {
            return json.stringify(String.serializer().list, value)
        }

        override fun deserialize(value: String): List<String>? {
            return json.parse(String.serializer().list, value)
        }
    }
}