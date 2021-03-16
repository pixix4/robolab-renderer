package de.robolab.client.utils.cache

import de.robolab.client.app.model.file.File
import de.robolab.client.utils.Electron
import de.robolab.client.utils.electron
import de.robolab.common.utils.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual class PersistentCacheStorage : ICacheStorage {

    private val path = PersistentCacheStorage.path

    override fun createEntryId(): CacheStorageId {
        return MemoryCacheStorage.generateCacheStorageId()
    }

    private fun CacheStorageId.toFile() = path.resolveChildren(id.replace("""[/\\]""".toRegex(), "") + ".cache")

    override suspend fun readEntry(id: CacheStorageId): String? {
        val file = id.toFile()
        return if (file.exists()) {
            file.readText()
        } else null
    }

    override suspend fun writeEntry(id: CacheStorageId, content: String?) {
        val file = id.toFile()
        if (file.exists()) {
            if (content == null) {
                file.delete()
            } else {
                file.writeText(content)
            }
        } else {
            if (content != null) {
                file.writeText(content)
            }
        }
    }

    override suspend fun clear() {
        for (file in path.listFiles()) {
            file.delete()
        }
    }

    companion object {

        private val logger = Logger("PersistentCacheStorage")

        private val path by lazy {
            when (electron?.getOs()) {
                Electron.OS.WINDOWS -> getDefaultWinCache()
                Electron.OS.MAC -> getDefaultMacCache()
                Electron.OS.LINUX -> getDefaultUnixCache()
                else -> getDefaultOtherCache()
            }.also { cache ->
                try {
                    if (!cache.exists()) {
                        GlobalScope.launch {
                            cache.createDirectories()
                        }
                    }
                } catch (_: Exception) {
                    logger.error("Cannot create default cache directory")
                }
            }
        }

        private fun getDefaultWinCache(): File =
            File(electron!!.appGetPath(Electron.PathName.APP_DATA)).resolveChildren("robolab", "cache")

        private fun getDefaultMacCache(): File =
            getDefaultUnixCache()

        private fun getDefaultUnixCache(): File =
            File(electron!!.appGetPath(Electron.PathName.HOME)).resolveChildren(".config", "robolab", "cache")

        private fun getDefaultOtherCache(): File =
            File(electron!!.appGetPath(Electron.PathName.HOME)).resolveChildren("robolab-cache")
    }
}
