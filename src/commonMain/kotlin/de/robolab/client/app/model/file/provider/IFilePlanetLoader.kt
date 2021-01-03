package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.SearchRequest
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler

interface IFilePlanetLoader<T: IFilePlanetIdentifier> {

    val onRemoteChange: EventHandler<T?>

    suspend fun loadPlanet(identifier: T): Pair<T, List<String>>?

    suspend fun savePlanet(identifier: T, lines: List<String>): T?

    suspend fun createPlanet(identifier: T?, lines: List<String>)

    suspend fun deletePlanet(identifier: T)

    suspend fun listPlanets(identifier: T? = null): List<T>

    suspend fun searchPlanets(search: String, matchExact: Boolean = false): List<T>

    val nameProperty: ObservableValue<String>
    val descProperty: ObservableValue<String>

    val planetCountProperty: ObservableValue<Int>

    val iconProperty: ObservableValue<MaterialIcon>

    val availableProperty: ObservableValue<Boolean>
}

interface IFilePlanetIdentifier {

    val isDirectory: Boolean

    val childrenCount: Int

    val name: String

    val lastModified: DateTime

    val path: List<String>

    fun matchesSearch(request: SearchRequest, planet: Planet): Boolean = request.matches(planet)
}

interface IFilePlanetLoaderFactory {
    fun create(uri: String): IFilePlanetLoader<*>?
}
