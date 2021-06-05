package de.robolab.client.app.model.file.provider

import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.externaljs.fs.readFileSync
import de.robolab.common.externaljs.fs.statSync
import de.robolab.common.externaljs.fs.writeFileSync
import de.robolab.common.jsutils.toDateTime
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetFile

actual fun loadTempFile(id: String): Pair<RemoteMetadata.Planet, Planet>? {
    if (existsSync(id)) {
        val content = readFileSync(id, js("{}")).toString()
        val planet = PlanetFile(content).planet
        val metadata = RemoteMetadata.Planet(
            planet.name,
            statSync(id).mtime.toDateTime(),
            planet.tags,
            planet.getPointList().size
        )
        return metadata to planet
    }

    return null
}

actual fun saveTempFile(id: String, content: Planet): RemoteMetadata.Planet? {
    if (existsSync(id)) {
        writeFileSync(id, PlanetFile.stringify(content))

        return RemoteMetadata.Planet(
            content.name,
            statSync(id).mtime.toDateTime(),
            content.tags,
            content.getPointList().size
        )
    }

    return null
}
