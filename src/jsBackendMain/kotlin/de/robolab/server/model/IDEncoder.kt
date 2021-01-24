package de.robolab.server.model

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.planet.ID
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.utils.decodeFromB64
import de.robolab.common.externaljs.Buffer
import de.robolab.common.net.data.DirectoryInfo

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

fun PlanetJsonInfo.asServerPlanetInfo(): ServerPlanetInfo = ServerPlanetInfo(
    id.decode(), name,
    this.lastModified, this.tags
)

fun ServerPlanetInfo.asPlanetJsonInfo(): PlanetJsonInfo = PlanetJsonInfo(id.toID(), name, lastModified, this.tags)

fun DirectoryInfo.ContentInfo.asServerDirectoryContentInfo(): DirectoryInfo.ServerContentInfo =
    DirectoryInfo.ServerContentInfo(path, lastModified, subdirectories, planets.map(PlanetJsonInfo::asServerPlanetInfo))

fun DirectoryInfo.ServerContentInfo.asDirectoryContentInfo(): DirectoryInfo.ContentInfo =
    DirectoryInfo.ContentInfo(path, lastModified, subdirectories, planets.map(ServerPlanetInfo::asPlanetJsonInfo))
