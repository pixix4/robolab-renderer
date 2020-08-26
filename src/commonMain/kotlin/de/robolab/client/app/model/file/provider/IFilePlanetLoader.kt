package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.SearchRequest
import de.robolab.common.planet.Planet
import de.westermann.kobserve.event.EventHandler

interface IFilePlanetLoader<T: IFilePlanetIdentifier> {

    val onRemoteChange: EventHandler<Unit>

    suspend fun loadContent(identifier: T): Pair<T, List<String>>?

    suspend fun saveContent(identifier: T, lines: List<String>): T?

    suspend fun createWithContent(lines: List<String>)

    suspend fun deleteIdentifier(identifier: T)

    suspend fun loadIdentifierList(): List<T>

    val name: String
    val desc: String

    val icon: MaterialIcon
}

interface IFilePlanetIdentifier {

    val name: String

    val lastModified: DateTime

    fun matchesSearch(request: SearchRequest, planet: Planet): Boolean = request.matches(planet)
}

interface IFilePlanetLoaderFactory {

    val usage: String

    val protocol: String

    fun create(uri: String): IFilePlanetLoader<*>?
}
