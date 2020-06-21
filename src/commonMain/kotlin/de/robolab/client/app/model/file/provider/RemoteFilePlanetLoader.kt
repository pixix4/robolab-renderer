package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.http
import de.robolab.client.net.requests.getPlanet
import de.robolab.client.net.requests.listPlanets
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
            val result = http {
                import(server)
                appendPath("/api/planets/${identifier.id}")
                put()
                body(lines.joinToString("\n"))
            }.exec()

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

    object HttpFactory: IFilePlanetLoaderFactory {

        override val protocol = "http"

        override val usage: String = "$protocol://example.org"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            val host = uri.substringAfter("$protocol://")
            return RemoteFilePlanetLoader(RESTRobolabServer(host, 0, false))
        }
    }

    object HttpsFactory: IFilePlanetLoaderFactory {

        override val protocol = "https"

        override val usage: String = "$protocol://example.org"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            val host = uri.substringAfter("$protocol://")
            return RemoteFilePlanetLoader(RESTRobolabServer(host, 0, true))
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
