package de.robolab.client.utils.cache

interface ICacheStorage {

    fun createEntryId(): CacheStorageId
    suspend fun readEntry(id: CacheStorageId): String?
    suspend fun writeEntry(id: CacheStorageId, content: String?)
    suspend fun clear()

    suspend fun readEntry(vararg path: String) = readEntry(CacheStorageId(*path))
    suspend fun writeEntry(vararg path: String, content: String?) = writeEntry(CacheStorageId(*path), content)

    fun getEntry(id: CacheStorageId): Entry {
        return Entry(this, id)
    }

    fun getEntry(vararg path: String): Entry {
        return Entry(this, CacheStorageId(*path))
    }

    fun createEntry(): Entry {
        return getEntry(createEntryId())
    }

    class Entry(
        private val storage: ICacheStorage,
        val id: CacheStorageId
    ) {

        suspend fun read(): String? {
            return storage.readEntry(id)
        }

        suspend fun write(content: String?) {
            return storage.writeEntry(id, content)
        }
    }
}
