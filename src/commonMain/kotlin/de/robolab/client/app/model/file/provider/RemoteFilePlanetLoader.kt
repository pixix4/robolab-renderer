package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.ICredentialProvider
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.requests.*
import de.robolab.common.net.HttpStatusCode
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteFilePlanetLoader(
    private val server: IRobolabServer
) : IFilePlanetLoader<PlanetJsonInfo> {

    override val onRemoteChange = EventHandler<Unit>()

    override val nameProperty = constObservable("Remote")

    override val descProperty = constObservable(server.hostURL)

    override val iconProperty = constObservable(MaterialIcon.CLOUD_QUEUE)

    override val availableProperty = constObservable(true)

    override suspend fun loadPlanet(identifier: PlanetJsonInfo): Pair<PlanetJsonInfo, List<String>>? {
        return withContext(Dispatchers.Default) {
            val result = server.getPlanet(identifier.id)
            if (result.status != HttpStatusCode.Ok) {
                null
            } else {
                PlanetJsonInfo(identifier.id, identifier.name, result.lastModified, identifier.tags) to result.lines
            }
        }
    }

    override suspend fun savePlanet(identifier: PlanetJsonInfo, lines: List<String>): PlanetJsonInfo? {
        return withContext(Dispatchers.Default) {
            val result = server.putPlanet(identifier.id, lines.joinToString("\n"))

            if (result.status != HttpStatusCode.Ok) null else identifier
        }
    }

    override suspend fun createPlanet(lines: List<String>) {
        return withContext(Dispatchers.Default) {
            server.postPlanet(lines.joinToString("\n"))
        }
    }

    override suspend fun deletePlanet(identifier: PlanetJsonInfo) {
        return withContext(Dispatchers.Default) {
            server.deletePlanet(identifier.id)
        }
    }

    override suspend fun listPlanets(identifier: PlanetJsonInfo?): List<PlanetJsonInfo> {
        return withContext(Dispatchers.Default) {
            server.listPlanets().planets
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
