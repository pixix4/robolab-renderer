package de.robolab.common.net.data

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.planet.ServerPlanetInfo
import kotlinx.serialization.Serializable

@Serializable
data class DirectoryInfo(
    val path: String,
    val subdirectories: List<String>,
    val planets: List<PlanetJsonInfo>
)

data class ServerDirectoryInfo(
    val path: String,
    val subdirectories: List<String>,
    val planets: List<ServerPlanetInfo>
)
