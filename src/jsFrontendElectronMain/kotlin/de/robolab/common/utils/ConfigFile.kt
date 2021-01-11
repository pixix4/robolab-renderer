package de.robolab.common.utils

import de.robolab.client.app.model.file.File
import de.robolab.client.utils.Electron
import de.robolab.client.utils.electron
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ConfigFile {

    private val logger = Logger("ConfigFile")

    private val path by lazy {
        when (electron?.getOs()) {
            Electron.OS.WINDOWS -> getDefaultWinConfig()
            Electron.OS.MAC -> getDefaultMacConfig()
            Electron.OS.LINUX -> getDefaultUnixConfig()
            else -> getDefaultOtherConfig()
        }.also { config ->
            try {
                if (!config.parent.exists()) {
                    GlobalScope.launch {
                        config.parent.createDirectories()
                    }
                }
            } catch (_: Exception) {
                logger.error("Cannot create default config directory")
            }
        }
    }

    private fun getDefaultWinConfig(): File =
        File(electron!!.appGetPath(Electron.PathName.APP_DATA)).resolveChildren("robolab", "config.ini")

    private fun getDefaultMacConfig(): File =
        getDefaultUnixConfig()

    private fun getDefaultUnixConfig(): File =
        File(electron!!.appGetPath(Electron.PathName.HOME)).resolveChildren(".config", "robolab", "config.ini")

    private fun getDefaultOtherConfig(): File =
        File(electron!!.appGetPath(Electron.PathName.HOME)).resolveChildren("robolab.conf")

    fun readSystemConfig(): String {
        try {
            return path.readTextSync()
        } catch (e: Exception) {
            logger.warn("Cannot read config file ${path.absolutePath}: ${e.message}")
        }
        return ""
    }

    fun writeSystemConfig(config: String) {
        try {
            path.writeTextSync(config)
        } catch (e: Exception) {
            logger.warn("Cannot write config file ${path.absolutePath}: ${e.message}")
        }
    }
}
