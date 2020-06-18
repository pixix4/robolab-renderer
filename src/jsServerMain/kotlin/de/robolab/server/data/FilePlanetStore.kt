package de.robolab.server.data

import com.soywiz.klock.js.toDateTime
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.planet.randomName
import de.robolab.server.RequestError
import de.robolab.server.externaljs.fs.*
import de.robolab.server.externaljs.os.EOL
import de.robolab.server.externaljs.path.safeJoinPath
import de.robolab.server.externaljs.toList
import de.robolab.server.jsutils.jsTruthy
import de.robolab.server.model.toIDString
import kotlinx.coroutines.await
import kotlin.js.Promise
import de.robolab.server.model.ServerPlanet as SPlanet


private suspend fun makeClosestFile(
    directory: String,
    name: String,
    postfix: String = ".planet"
): Pair<String, FileHandle> {
    var currentFileBaseName = name
    var currentFile = safeJoinPath(directory, currentFileBaseName + postfix)
    var counter = 0

    fun catchHandler(err: dynamic): Promise<Any?> {
        if (!jsTruthy(err)) throw IllegalStateException("open failed with falsy error: $err")
        if (err.code != "EEXIST") throw err
        counter++
        currentFileBaseName = "$name-$counter"
        currentFile = safeJoinPath(directory, currentFileBaseName + postfix)
        return open(currentFile, "wx").catch(::catchHandler)
    }

    //prom: T where T:Union<FileHandle,Promise<T>>
    var prom: Any? = open(currentFile, "wx").catch(::catchHandler)

    while (prom is Promise<*>) {
        prom = prom.await()
    }
    return Pair(currentFileBaseName, prom.unsafeCast<FileHandle>())
}

class FilePlanetStore(val directory: String, val metaStore: IPlanetMetaStore) : IPlanetStore {

    private fun getPath(id: String): String = safeJoinPath(directory, "$id.planet")

    override suspend fun add(planet: SPlanet.Template): SPlanet {
        val (name: String, handle: FileHandle) = makeClosestFile(directory, planet.name)
        try {
            handle.writeFile(planet.lines.joinToString(EOL)).await()
        } finally {
            handle.close()
        }
        val resultPlanet = planet.withID(name)
        metaStore.setInfo(resultPlanet.info.value)
        return resultPlanet
    }

    override suspend fun remove(planet: SPlanet): Boolean {
        val oldInfo: ServerPlanetInfo = getInfo(planet.id) ?: return false
        if (oldInfo == planet.info.value) {
            removeBlind(oldInfo.id)
            return true
        }
        return false
    }

    override suspend fun removeBlind(id: String) {
        metaStore.removeInfoByID(id)
        try {
            unlink(getPath(id)).await()
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT")
                throw ex.unsafeCast<Throwable>()
        }
    }

    override suspend fun remove(id: String): SPlanet? {
        val oldPlanet: SPlanet? = get(id)
        metaStore.removeInfoByID(id)
        if (oldPlanet == null) return null
        try {
            unlink(getPath(id)).await()
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT")
                throw ex.unsafeCast<Throwable>()
        }
        return oldPlanet
    }

    override suspend fun update(planet: SPlanet) {
        val path = getPath(planet.id)
        val stat = stat(path).await()
        if (stat.isFile()) {
            val handle: FileHandle = open(path, "w").await()
            try {
                handle.writeFile(planet.planetFile.content).await()
            } finally {
                handle.close()
            }
        }

        metaStore.setInfo(planet.info.value, onlyIfExist = true)
    }

    override suspend fun get(id: String): SPlanet? {
        var planetFile: PlanetFile? = null
        if (id.contains('\u0000'))
            throw RequestError(HttpStatusCode.UnprocessableEntity, "Invalid id: \"${id.toIDString()}\"")
        val path: String = getPath(id)
        val metadata: ServerPlanetInfo? = metaStore.retrieveInfo(id) {
            val localPlanetFile: PlanetFile?
            try {
                localPlanetFile = readPlanetFile(id)
            } catch (ex: dynamic) {
                if (ex.code != "ENOENT")
                    throw ex.unsafeCast<Throwable>()
                return@retrieveInfo null
            }
            if (localPlanetFile != null) {
                planetFile = localPlanetFile
                return@retrieveInfo ServerPlanetInfo(
                    id,
                    planetFile!!.planet.name,
                    stat(path).await().mtime.toDateTime()
                )
            }
            return@retrieveInfo null
        }
        if (planetFile == null) {
            val content: String
            try {
                content = readFile(path).await()
            } catch (ex: dynamic) {
                if (ex.code != "ENOENT")
                    throw ex.unsafeCast<Throwable>()
                return null
            }
            planetFile = PlanetFile(content)
        }
        val name = if (planetFile!!.planet.name.isEmpty() && metadata != null)
            metadata.name
        else
            planetFile!!.planet.name
        val info = metadata ?: ServerPlanetInfo(id, name, stat(path).await().mtime.toDateTime())
        return SPlanet(info, lines = planetFile!!.content.split("""\r?\n""".toRegex()))
    }

    override suspend fun getInfo(id: String): ServerPlanetInfo? {
        return metaStore.retrieveInfo(id, ::lookupInfo)
    }

    private suspend fun lookupInfo(id: String): ServerPlanetInfo? {
        return ServerPlanetInfo(
            id,
            readPlanetFile(id)?.planet?.name ?: randomName(),
            stat(getPath(id)).await().mtime.toDateTime()
        )
    }

    private suspend fun readPlanetFile(id: String): PlanetFile? {
        val content = readFile(getPath(id)).await()
        return PlanetFile(content)
    }

    override suspend fun listPlanets(): List<ServerPlanetInfo> {
        return metaStore.retrieveInfo(
            readdirents(directory).await().toList()
                .filter { dirent -> dirent.isFile() and dirent.name.endsWith(".planet") }
                .map { dirent -> dirent.name.split('.').dropLast(1).joinToString(".") },
            ::lookupInfo
        ).filterNotNull()
    }

}