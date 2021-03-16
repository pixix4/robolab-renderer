package de.robolab.client.utils.cache

import com.soywiz.klock.DateTime
import de.robolab.client.traverser.nextHexString
import kotlin.random.Random

class MemoryCacheStorage : ICacheStorage {

    private val storage = mutableMapOf<CacheStorageId, String>()

    override fun createEntryId(): CacheStorageId {
        while (true) {
            val id = generateCacheStorageId()
            if (id !in storage) return id
        }
    }

    override suspend fun readEntry(id: CacheStorageId): String? {
        return storage[id]
    }

    override suspend fun writeEntry(id: CacheStorageId, content: String?) {
        if (content == null) {
            storage -= id
        } else {
            storage[id] = content
        }
    }

    override suspend fun clear() {
        storage.clear()
    }

    companion object {
        fun generateCacheStorageId(): CacheStorageId {
            return CacheStorageId(
                "temp",
                DateTime.nowUnixLong().toString(),
                Random.nextHexString(8),
                Random.nextHexString(8),
                Random.nextHexString(8),
            )
        }
    }
}
