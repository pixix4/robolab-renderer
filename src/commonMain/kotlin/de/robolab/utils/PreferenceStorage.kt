package de.robolab.utils

import de.robolab.app.controller.SideBarController
import de.robolab.renderer.theme.Theme
import de.westermann.kobserve.Binding
import de.westermann.kobserve.Property
import de.westermann.kobserve.event.EventHandler
import kotlin.random.Random

object PreferenceStorage {
    private val storage = KeyValueStorage()

    val selectedThemeProperty = item("THEME", Theme.LIGHT)
    var selectedTheme by selectedThemeProperty

    val useSystemThemeProperty = item("USE_SYSTEM_THEME", true)
    var useSystemTheme by useSystemThemeProperty

    val exportScaleProperty = item("EXPORT_SCALE", 4.0)
    var exportScale by exportScaleProperty

    val selectedSideBarTabProperty = item("SIDE_BAR_TAB", SideBarController.Tab.FILE)
    var selectedSideBarTab by selectedSideBarTabProperty
    
    val clientIdProperty = item("CLIENT_ID", Random.nextBytes(16).joinToString("") { it.toString(16).padStart(2, '0') })
    var clientId by clientIdProperty

    val serverUriProperty = item("SERVER_URI", "ssl://mothership.inf.tu-dresden.de:8883")
    var serverUri by serverUriProperty

    val usernameProperty = item("USERNAME", "")
    var username by usernameProperty

    val passwordProperty = item("PASSWORD", "")
    var password by passwordProperty

    abstract class Item<T>(private val key: String, val default: T) : Property<T> {
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
                storage[key] = serialize(value)
                onChange.emit(Unit)
            }
        }
    }

    private fun item(key: String, default: Double): Item<Double> = DoubleItem(key, default)
    private class DoubleItem(key: String, default: Double) : Item<Double>(key, default) {
        override fun serialize(value: Double): String? {
            return value.toString()
        }

        override fun deserialize(value: String): Double? {
            return value.toDoubleOrNull()
        }
    }

    private fun item(key: String, default: String): Item<String> = StringItem(key, default)
    private class StringItem(key: String, default: String) : Item<String>(key, default) {
        override fun serialize(value: String): String? {
            return value
        }

        override fun deserialize(value: String): String? {
            return value
        }
    }

    private fun item(key: String, default: Boolean): Item<Boolean> = BooleanItem(key, default)
    private class BooleanItem(key: String, default: Boolean) : Item<Boolean>(key, default) {
        override fun serialize(value: Boolean): String? {
            return value.toString()
        }

        override fun deserialize(value: String): Boolean? {
            return value.toLowerCase() == "true"
        }
    }

    private inline fun <reified T : Enum<T>> item(key: String, default: T): Item<T> = EnumItem(key, default, enumValues())
    private class EnumItem<T : Enum<T>>(key: String, default: T, private val valueList: Array<T>) : Item<T>(key, default) {
        override fun serialize(value: T): String? {
            return value.name
        }
        override fun deserialize(value: String): T? {
            return valueList.find { it.name == value }
        }

    }
}
