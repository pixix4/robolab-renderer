package de.robolab.server.model

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.planet.ID
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.server.externaljs.Buffer

const val IDEncoding: String = "ascii"

fun ID.decode(): String = Buffer.from(
    id
        .replace('-', '+')
        .replace('_', '/')
        .replace("%3d", "="), "base64"
).toString(
    IDEncoding
)

fun String.toID(): ID = ID(
    Buffer.from(
        this,
        IDEncoding
    ).toString("base64")
        .replace('+', '-')
        .replace('/', '_')
        .replace("""=+$""".toRegex(), "") //removes all trailing '=', which should be all
        .replace("=", "%3d")
)

fun String.toIDString(): String = this.toID().id
fun String.decodeID(): String = ID(this).decode()

fun PlanetJsonInfo.asServerPlanetInfo(): ServerPlanetInfo = ServerPlanetInfo(id.decode(), name,
    this.lastModified
)

fun ServerPlanetInfo.asPlanetJsonInfo(): PlanetJsonInfo = PlanetJsonInfo(id.toID(), name, lastModified)