package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.ICredentialProvider
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.requests.getPlanet
import de.robolab.client.net.requests.listPlanets
import de.robolab.client.net.requests.putPlanet
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.planet.ID
import de.westermann.kobserve.event.EventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteFilePlanetLoader(
    private val server: IRobolabServer
) : IFilePlanetLoader<RemoteFilePlanetLoader.PlanetJsonInfo> {

    override val onRemoteChange = EventHandler<Unit>()

    override val name = "Remote"

    override val desc = server.hostURL

    override val icon = MaterialIcon.CLOUD_QUEUE

    override suspend fun loadContent(identifier: PlanetJsonInfo): Pair<PlanetJsonInfo, List<String>>? {
        return withContext(Dispatchers.Default) {
            val result = server.getPlanet(identifier.id)
            if (result.status != HttpStatusCode.Ok) null else
                PlanetJsonInfo(identifier.id, identifier.name, result.lastModified) to result.lines
        }
    }

    override suspend fun saveContent(identifier: PlanetJsonInfo, lines: List<String>): PlanetJsonInfo? {
        return withContext(Dispatchers.Default) {
            val result = server.putPlanet(identifier.id, lines.joinToString("\n"))

            if (result.status != HttpStatusCode.Ok) null else identifier
        }
    }

    override suspend fun loadIdentifierList(): List<PlanetJsonInfo> {
        return withContext(Dispatchers.Default) {
            server.listPlanets().planets.map {
                PlanetJsonInfo(
                    it.id,
                    it.name,
                    it.lastModifiedDate
                )
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

    class PlanetJsonInfo(
        val id: ID,
        override val name: String,
        override val lastModified: DateTime
    ) : IFilePlanetIdentifier {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PlanetJsonInfo) return false

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }
}
