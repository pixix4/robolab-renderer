package de.robolab.server.config

import de.robolab.common.utils.TypedStorage
import de.robolab.server.externaljs.env

open class EnvTypedStorage : TypedStorage() {
    override fun item(key: String, default: Double): Item<Double> = DoubleItem(key, default)

    protected open inner class DoubleItem(key: String, default: Double) : TypedStorage.DoubleItem(key, default) {
        override fun set(value: Double) {
            val raw = getRaw()
            if (raw != null && raw.startsWith('$')) throw IllegalStateException("Cannot set value stored in env-variable")
            super.set(value)
        }

        override fun deserialize(value: String): Double? {
            return super.deserialize(value.let {
                if (it.startsWith("$")) {
                    env[it.substring(1)] as String? ?: return null
                } else it
            })
        }
    }

    override fun item(key: String, default: Int): Item<Int> = IntItem(key, default)

    protected open inner class IntItem(key: String, default: Int) : TypedStorage.IntItem(key, default) {
        override fun set(value: Int) {
            val raw = getRaw()
            if (raw != null && raw.startsWith('$')) throw IllegalStateException("Cannot set value stored in env-variable")
            super.set(value)
        }

        override fun deserialize(value: String): Int? {
            return super.deserialize(value.let {
                if (it.startsWith("$")) {
                    env[it.substring(1)] as String? ?: return null
                } else it
            })
        }
    }

    override fun item(key: String, default: String): Item<String> = StringItem(key, default)

    protected open inner class StringItem(key: String, default: String) : TypedStorage.StringItem(key, default) {
        override fun set(value: String) {
            val raw = getRaw()
            if (raw != null && raw.startsWith('$')) throw IllegalStateException("Cannot set value stored in env-variable")
            super.set(value)
        }

        override fun deserialize(value: String): String? {
            return super.deserialize(value.let {
                if (it.startsWith("$")) {
                    env[it.substring(1)] as String? ?: return null
                } else it
            })
        }
    }

    override fun item(key: String, default: Boolean): Item<Boolean> = BooleanItem(key, default)

    protected open inner class BooleanItem(key: String, default: Boolean) : TypedStorage.BooleanItem(key, default) {
        override fun set(value: Boolean) {
            val raw = getRaw()
            if (raw != null && raw.startsWith('$')) throw IllegalStateException("Cannot set value stored in env-variable")
            super.set(value)
        }

        override fun deserialize(value: String): Boolean? {
            return super.deserialize(value.let {
                if (it.startsWith("$")) {
                    env[it.substring(1)] as String? ?: return null
                } else it
            })
        }
    }

    override fun <T : Enum<T>> item(key: String, default: T, valueList: Array<T>): Item<T> =
        EnumItem(key, default, valueList)

    protected open inner class EnumItem<T : Enum<T>>(key: String, default: T, valueList: Array<T>) :
        TypedStorage.EnumItem<T>(key, default, valueList) {
        override fun set(value: T) {
            val raw = getRaw()
            if (raw != null && raw.startsWith('$')) throw IllegalStateException("Cannot set value stored in env-variable")
            super.set(value)
        }

        override fun deserialize(value: String): T? {
            return super.deserialize(value.let {
                if (it.startsWith("$")) {
                    env[it.substring(1)] as String? ?: return null
                } else it
            })
        }
    }

}