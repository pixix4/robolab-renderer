package de.robolab.client.utils

import kotlinx.browser.window

class Electron private constructor() {

    companion object {
        private fun isElectron(): Boolean {
            val w = window.asDynamic()

            if (w.process != undefined && w.process.type == "renderer") {
                return true
            }

            return false
        }

        val instance by lazy {
            if (isElectron()) Electron() else null
        }
    }
}

val isElectron by lazy { Electron.instance != null }

val electron by lazy { Electron.instance }

inline fun electron(block: (Electron) -> Unit) {
    val e = Electron.instance
    if (e != null) {
        block(e)
    }
}

inline fun noElectron(block: () -> Unit) {
    val e = Electron.instance
    if (e == null) {
        block()
    }
}