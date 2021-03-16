package de.robolab.client.utils.cache

actual class PersistentCacheStorage: ICacheStorage {

    override fun createEntryId(): CacheStorageId {
        throw NotImplementedError()
    }

    override suspend fun readEntry(id: CacheStorageId): String? {
        throw NotImplementedError()
    }

    override suspend fun writeEntry(id: CacheStorageId, content: String?) {
        throw NotImplementedError()
    }

    override suspend fun clear() {
        throw NotImplementedError()
    }
}
