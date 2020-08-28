package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.ICredentialProvider
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.requests.*
import de.robolab.common.net.HttpStatusCode
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteFilePlanetLoader(
    private val server: IRobolabServer
) : IFilePlanetLoader<PlanetJsonInfo> {

    override val onRemoteChange = EventHandler<Unit>()

    override val nameProperty = constObservable("Remote")

    override val descProperty = constObservable(server.hostURL)

    override val iconProperty = constObservable(MaterialIcon.CLOUD_QUEUE)

    override val availableProperty = property(true)
    var available by availableProperty

    override suspend fun loadPlanet(identifier: PlanetJsonInfo): Pair<PlanetJsonInfo, List<String>>? {
        return withContext(Dispatchers.Default) {
            try {
                val result = server.getPlanet(identifier.id)
                if (result.status != HttpStatusCode.Ok) {
                    available = false
                    null
                } else {
                    available = true
                    PlanetJsonInfo(identifier.id, identifier.name, result.lastModified, identifier.tags) to result.lines
                }
            } catch (e: Exception) {
                available = false
                null
            }
        }
    }

    override suspend fun savePlanet(identifier: PlanetJsonInfo, lines: List<String>): PlanetJsonInfo? {
        return withContext(Dispatchers.Default) {
            try {
                val result = server.putPlanet(identifier.id, lines.joinToString("\n"))

                if (result.status != HttpStatusCode.Ok) {
                    available = false
                    null
                } else {
                    available = true
                    identifier
                }
            } catch (e: Exception) {
                available = false
                null
            }
        }
    }

    override suspend fun createPlanet(lines: List<String>) {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                server.postPlanet(lines.joinToString("\n"))
            } catch (e: Exception) {
                available = false
            }
        }
    }

    override suspend fun deletePlanet(identifier: PlanetJsonInfo) {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                server.deletePlanet(identifier.id)
            } catch (e: Exception) {
                available = false
            }
        }
    }

    override suspend fun listPlanets(identifier: PlanetJsonInfo?): List<PlanetJsonInfo> {
        return withContext(Dispatchers.Default) {
            try {
                available = true
                server.listPlanets().planets
            } catch (e: Exception) {
                available = false
                emptyList()
            }
        }
    }

    private data class Auth(
        override val username: String,
        override val password: String
    ) : ICredentialProvider

    object HttpFactory : IFilePlanetLoaderFactory {

        override val protocol = "http"

        override val usage: String = "$protocol://[username:password@]example.org"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            val (host, auth) = HttpsFactory.parse(uri)
            val restRobolabServer = RESTRobolabServer(host, 0, false)
            if (auth != null) {
                restRobolabServer.credentials = auth
            }
            return RemoteFilePlanetLoader(restRobolabServer)
        }
    }

    object HttpsFactory : IFilePlanetLoaderFactory {

        override val protocol = "https"

        override val usage: String = "$protocol://[username:password@]example.org"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            val (host, auth) = parse(uri)
            val restRobolabServer = RESTRobolabServer(host, 0, true)
            if (auth != null) {
                restRobolabServer.credentials = auth
            }
            return RemoteFilePlanetLoader(restRobolabServer)
        }

        fun parse(uri: String): Pair<String, ICredentialProvider?> {
            val uriContent = uri.substringAfter("://")
            return if ("@" in uriContent) {
                val (auth, host) = uriContent.split('@', limit = 2)
                val authSplit = auth.split(':', limit = 2)
                val username = authSplit.getOrNull(0) ?: ""
                val password = authSplit.getOrNull(1) ?: ""
                host to Auth(username, password)
            } else {
                uriContent to null
            }
        }
    }

}
