package de.robolab.server.data

import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ID
import de.robolab.common.planet.PlanetInfo
import de.robolab.server.externaljs.fs.Dirent
import de.robolab.server.externaljs.fs.readFile
import de.robolab.server.externaljs.fs.readdir
import de.robolab.server.externaljs.fs.readdirents
import de.robolab.server.externaljs.path.join
import de.robolab.server.externaljs.toList
import de.robolab.server.model.toID
import kotlinx.coroutines.await
import kotlin.js.Date
import kotlin.reflect.typeOf
import de.robolab.server.model.ServerPlanet as SPlanet

class FilePlanetStore(val directory: String, val metaStore: IPlanetMetaStore) : IPlanetStore {

    override suspend fun add(planet: SPlanet): ID? =
        "SUCH-RANDOM-VERY-ID-WOW".toID()

    override suspend fun remove(planet: SPlanet): Boolean = false

    override suspend fun remove(id: ID): SPlanet? = null

    override suspend fun get(id: ID): SPlanet? = SPlanet()

    override suspend fun get(name: String): List<SPlanet> = listOf(SPlanet(), SPlanet())

    override suspend fun listPlanets(): List<PlanetInfo> {
        return metaStore.retrieveInfo(
            readdirents(directory).await().toList()
                .filter { dirent -> dirent.isFile() and dirent.name.endsWith(".planet") }
                .map { dirent -> dirent.name.split('.').dropLast(1).joinToString(".") })
        {
            val content = readFile(join(directory, "$it.planet")).await()
            PlanetInfo(it.toID(), PlanetFile(content).planet.name)
        }
    }

}