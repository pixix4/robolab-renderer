package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.SearchRequest
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler

interface IFilePlanetLoader<T: IFilePlanetIdentifier> {

    val onRemoteChange: EventHandler<Unit>

    suspend fun loadPlanet(identifier: T): Pair<T, List<String>>?

    suspend fun savePlanet(identifier: T, lines: List<String>): T?

    suspend fun createPlanet(lines: List<String>)

    suspend fun deletePlanet(identifier: T)

    suspend fun listPlanets(identifier: T? = null): List<T>

    val nameProperty: ObservableValue<String>
    val descProperty: ObservableValue<String>

    val iconProperty: ObservableValue<MaterialIcon>

    val availableProperty: ObservableValue<Boolean>
}

interface IFilePlanetIdentifier {

    val isDirectory: Boolean

    val name: String

    val lastModified: DateTime

    fun matchesSearch(request: SearchRequest, planet: Planet): Boolean = request.matches(planet)
}

interface IFilePlanetLoaderFactory {

    val usage: String

    val protocol: String

    fun create(uri: String): IFilePlanetLoader<*>?
}
