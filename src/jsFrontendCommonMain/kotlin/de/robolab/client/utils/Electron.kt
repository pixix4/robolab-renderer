package de.robolab.client.utils

import kotlinx.browser.window
import org.w3c.dom.events.Event

class Electron private constructor() {

    private val module = js("require('electron')")

    val ipcRenderer = module.ipcRenderer.unsafeCast<IpcRenderer>()

    fun appGetPath(name: PathName): String {
        return module.remote.app.getPath(name.value).unsafeCast<String>()
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

    fun openExternal(url: String) {
        module.shell.openExternal(url)
    }

    fun getOs(): OS {
        val process = js("require(\"process\")")
        val name = process.platform.unsafeCast<String>()
        return when(name) {
            "win32" -> OS.WINDOWS
            "darwin" -> OS.MAC
            "linux" -> OS.LINUX
            else -> OS.OTHER
        }
    }

    fun getMemoryInfo(): BlinkMemoryInfo {
        val process = js("require(\"process\")")
        return process.getBlinkMemoryInfo().unsafeCast<BlinkMemoryInfo>()
    }

    enum class PathName(val value: String) {
        HOME("home"),
        APP_DATA("appData"),
        USER_DATA("userData"),
        CACHE("cache"),
        TEMP("temp"),
        EXE("exe"),
        MODULE("module"),
        DESKTOP("desktop"),
        DOCUMENTS("documents"),
        DOWNLOADS("downloads"),
        MUSIC("music"),
        PICTURES("pictures"),
        VIDEOS("videos"),
        RECENT("recent"),
        LOGS("logs"),
        PEPPER_FLASH_SYSTEM_PLUGIN("pepperFlashSystemPlugin"),
        CRASH_DUMPS("crashDumps")
    }

    enum class OS {
        WINDOWS, LINUX, MAC, OTHER
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

external interface BlinkMemoryInfo {
    val allocated: Int
    val marked: Int
    val total: Int
}
