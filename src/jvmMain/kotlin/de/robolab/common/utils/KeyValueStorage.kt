package de.robolab.common.utils

import de.robolab.client.jfx.MainApp
import java.util.prefs.Preferences

actual class KeyValueStorage {

    private val storage = Preferences.userNodeForPackage(MainApp::class.java)

    actual operator fun get(key: String): String? {
        return storage.get(key, null)
    }

    actual operator fun set(key: String, value: String?) {
        if (value == null) {
            storage.remove(key)
        } else {
            storage.put(key, value)
        }
    }

    actual operator fun contains(key: String): Boolean {
        return storage.get(key, null) != null
    }

    actual fun clear() {
        storage.clear()
    }
    
    actual fun keys(): Set<String> {
        return storage.keys().toSet()
    }
}
