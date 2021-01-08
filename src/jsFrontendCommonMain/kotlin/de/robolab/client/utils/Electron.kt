package de.robolab.client.utils

import kotlinx.browser.window
import org.w3c.dom.events.Event

class Electron private constructor() {

    val ipcRenderer = js("require('electron').ipcRenderer").unsafeCast<IpcRenderer>()

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

external class IpcRendererEvent(): Event {
    val sender: Any= definedExternally
    val senderId: Int= definedExternally
    val ports: Array<Any> = definedExternally
}

external interface IpcRenderer {
    fun on(channel: String, listener: (event: IpcRendererEvent, args: dynamic) -> Unit)
    fun once(channel: String, listener: (event: IpcRendererEvent, args: dynamic) -> Unit)
    fun send(channel: String, vararg args: dynamic)
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