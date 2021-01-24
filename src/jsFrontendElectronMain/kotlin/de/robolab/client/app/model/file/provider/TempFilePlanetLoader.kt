package de.robolab.client.app.model.file.provider

import com.soywiz.klock.js.toDateTime
import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.externaljs.fs.readFileSync
import de.robolab.common.externaljs.fs.statSync
import de.robolab.common.externaljs.fs.writeFileSync
import de.robolab.common.parser.PlanetFile

actual fun loadTempFile(id: String): Pair<RemoteMetadata.Planet, List<String>>? {
    if (existsSync(id)) {
        val content = readFileSync(id, js("{}")).toString().split("\n")
        val planet = PlanetFile(content).planet
        val metadata = RemoteMetadata.Planet(
            planet.name,
            statSync(id).mtime.toDateTime(),
            planet.tagMap,
            planet.getPointList().size
        )
        return metadata to content
    }

    return null
}

actual fun saveTempFile(id: String, content: List<String>): RemoteMetadata.Planet? {
    if (existsSync(id)) {
        writeFileSync(id, content.joinToString("\n"))

        val planet = PlanetFile(content).planet
        return RemoteMetadata.Planet(
            planet.name,
            statSync(id).mtime.toDateTime(),
            planet.tagMap,
            planet.getPointList().size
        )
    }

    return null
}

