package de.robolab.client.app.model.file.provider

actual fun loadTempFile(id: String): Pair<RemoteMetadata.Planet, List<String>>? = null
actual fun saveTempFile(id: String, content: List<String>): RemoteMetadata.Planet? = null
