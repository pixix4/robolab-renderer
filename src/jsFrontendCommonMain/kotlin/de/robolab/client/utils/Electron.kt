package de.robolab.client.utils

import kotlinx.browser.window
import org.w3c.dom.events.Event

class Electron private constructor() {

    val module = js("require('electron')")

    val ipcRenderer = module.ipcRenderer.unsafeCast<IpcRenderer>()

    init {
        console.log(module)
    }

    private fun menuItem(entry: ContextMenuEntry): dynamic {
        val menu = js("{}")
        menu.label = entry.label

        when (entry) {
            is ContextMenuAction -> {
                menu.click = entry.action
            }
            is ContextMenuList -> {
                menu.submenu = js("[]")

                for (e in entry.entries) {
                    menu.submenu.push(menuItem(e))
                }
            }
        }

        return menu
    }

    fun menu(contextMenu: ContextMenu) {
        val menuTemplate = js("[]")

        for (e in contextMenu.entry.entries) {
            menuTemplate.push(menuItem(e))
        }

        val menu = module.remote.Menu.buildFromTemplate(menuTemplate)

        val opt = js("{}")
        opt.window = module.remote.getCurrentWindow()
        menu.popup(opt)
    }


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

external class IpcRendererEvent() : Event {
    val sender: Any = definedExternally
    val senderId: Int = definedExternally
    val ports: Array<Any> = definedExternally
}

external interface IpcRenderer {
    fun on(channel: String, listener: (event: IpcRendererEvent, args: dynamic) -> Unit)
    fun once(channel: String, listener: (event: IpcRendererEvent, args: dynamic) -> Unit)
    fun send(channel: String, vararg args: dynamic)
}

val isElectron
    get() = Electron.instance != null

val electron
    get() = Electron.instance

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