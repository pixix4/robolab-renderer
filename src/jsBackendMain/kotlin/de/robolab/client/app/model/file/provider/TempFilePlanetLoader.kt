package de.robolab.client.app.model.file.provider

import de.robolab.common.planet.Planet

actual fun loadTempFile(id: String): Pair<RemoteMetadata.Planet, Planet>? = null
actual fun saveTempFile(id: String, content: Planet): RemoteMetadata.Planet? = null
