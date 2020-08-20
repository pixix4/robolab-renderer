package de.robolab.server.model

import de.robolab.common.planet.ClientPlanetInfo
import de.robolab.common.planet.ID
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.utils.decodeFromB64
import de.robolab.server.externaljs.Buffer

const val IDEncoding: String = "utf8"

fun ID.decode(): String = if (IDEncoding == "utf8") id.decodeFromB64(true) else Buffer.from(
    id.decodeFromB64(true), "utf8"
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

fun ClientPlanetInfo.asServerPlanetInfo(): ServerPlanetInfo = ServerPlanetInfo(id.decode(), name,
    this.lastModifiedDate
)
fun ServerPlanetInfo.asClientPlanetInfo(): ClientPlanetInfo = ClientPlanetInfo(id.toID(), name, lastModifiedDate)