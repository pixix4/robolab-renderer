package de.robolab.client.utils.cache

import kotlinx.serialization.Serializable

@Serializable
data class CacheStorageId(val id: String) {
    constructor(vararg path: String) : this(path.joinToString("."))
}
