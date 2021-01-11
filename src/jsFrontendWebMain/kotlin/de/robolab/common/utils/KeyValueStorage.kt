package de.robolab.common.utils

import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set

actual class KeyValueStorage {
    actual operator fun get(key: String): String? {
        return window.localStorage[key]
    }

    actual operator fun set(key: String, value: String?) {
        if (value == null) {
            window.localStorage.removeItem(key)
        } else {
            window.localStorage[key] = value
        }
    }

    actual operator fun contains(key: String): Boolean {
        return window.localStorage[key] != null
    }

    actual fun clear() {
        window.localStorage.clear()
    }

    actual fun keys(): Set<String> {
        val count = window.localStorage.length

        return (0 until count).mapNotNull { window.localStorage.key(it) }.toSet()
    }
}
