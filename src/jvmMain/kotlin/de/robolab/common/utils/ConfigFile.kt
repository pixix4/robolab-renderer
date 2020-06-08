package de.robolab.common.utils

import de.robolab.client.jfx.utils.SystemOs
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object ConfigFile {

    var localPath: Path? = null
    private val path by lazy {
        when (SystemOs.os) {
            SystemOs.OSType.Windows -> getDefaultWinConfig()
            SystemOs.OSType.MacOS -> getDefaultMacConfig()
            SystemOs.OSType.Linux -> getDefaultUnixConfig()
            SystemOs.OSType.Other -> Paths.get(System.getProperty("user.home")).resolve("robolab.conf")
        }.also { config ->
            try {
                Files.createDirectories(config.parent)
            } catch (_: Exception) {
                Logger("ConfigFile").error("Cannot create default config directory")
            }
        }
    }

    private fun getDefaultWinConfig(): Path =
        Paths.get(System.getenv("AppData"))
            .resolve("robolab")
            .resolve("config.ini")

    private fun getDefaultMacConfig(): Path =
        getDefaultUnixConfig()

    private fun getDefaultUnixConfig(): Path =
        Paths.get(System.getProperty("user.home"))
            .resolve(".config")
            .resolve("robolab")
            .resolve("config.ini")


    fun readSystemConfig(): String {
        val path = localPath ?: path
        try {
            if (Files.exists(path) && Files.isReadable(path) && !Files.isDirectory(path)) {
                return Files.readString(path)
            }
        } catch (e: Exception) {
            Logger("ConfigFile").w("Cannot read config file ${path.toAbsolutePath()}: ${e.message}")
        }
        return ""
    }

    fun writeSystemConfig(config: String) {
        val path = localPath ?: path
        try {
            if (!Files.exists(path) || Files.isWritable(path) && !Files.isDirectory(path)) {
                Files.writeString(path, config)
            }
        } catch (e: Exception) {
            Logger("ConfigFile").w("Cannot write config file ${path.toAbsolutePath()}: ${e.message}")
        }
    }
}

actual fun getBuildInformation(): String? {
    return ConfigFile::class.java.classLoader.getResource("build.ini")?.readText() ?: ""
}
actual suspend fun getAsyncBuildInformation(): String {
    return ConfigFile::class.java.classLoader.getResource("build.ini")?.readText() ?: ""
}
