package de.robolab.client.utils.cache

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

actual class PersistentCacheStorage : ICacheStorage {

    private val storage = localStorage

    override fun createEntryId(): CacheStorageId {
        return MemoryCacheStorage.generateCacheStorageId()
    }

    override suspend fun readEntry(id: CacheStorageId): String? {
        val file = "$PREFIX${id.id}"
        return storage[file]
    }

    override suspend fun writeEntry(id: CacheStorageId, content: String?) {
        val file = "$PREFIX${id.id}"
        if (content == null) {
            storage.removeItem(file)
        } else {
            storage[file] = content
        }
    }

    override suspend fun clear() {
        for (index in 0 until storage.length) {
            val key = storage.key(index)
            if (key != null && key.startsWith(PREFIX)) {
                storage.removeItem(key)
            }
        }
    }

    companion object {
        private const val PREFIX = "robolab-renderer-cache-"
    }
}
