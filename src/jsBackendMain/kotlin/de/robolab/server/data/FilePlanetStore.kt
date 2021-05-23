package de.robolab.server.data

import de.robolab.client.utils.removeCommon
import de.robolab.common.externaljs.fs.*
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.server.net.RESTResponseCodeException
import de.robolab.common.externaljs.os.EOL
import de.robolab.common.externaljs.path.pathJoin
import de.robolab.common.externaljs.path.safeJoinPath
import de.robolab.common.externaljs.toList
import de.robolab.common.jsutils.jsTruthy
import de.robolab.common.jsutils.toDateTime
import de.robolab.common.net.data.DirectoryInfo
import de.robolab.common.utils.Logger
import de.robolab.server.config.Config
import de.robolab.server.model.toIDString
import kotlinx.coroutines.await
import kotlin.js.Promise
import de.robolab.server.model.ServerPlanet as SPlanet

private suspend fun makeClosestFile(
    directory: String,
    name: String,
    path: String? = null,
    postfix: String = ".planet"
): Triple<String, FileHandle, String> {
    val unslashedPath: String? = when {
        path == null -> null
        (path.startsWith('/') || path.startsWith('\\')) && (path.endsWith('/') || path.endsWith('\\'))
        -> path.substring(1, path.length - 1)
        (path.startsWith('/') || path.startsWith('\\')) && !(path.endsWith('/') || path.endsWith('\\'))
        -> path.substring(1)
        (!(path.startsWith('/') || path.startsWith('\\'))) && (path.endsWith('/') || path.endsWith('\\'))
        -> path.substring(0, path.length - 1)
        else -> path
    }
    var currentFileBaseName = name
    var currentFile = if (!unslashedPath.isNullOrEmpty())
        safeJoinPath(directory, unslashedPath, currentFileBaseName + postfix)
    else
        safeJoinPath(directory, currentFileBaseName + postfix)
    var counter = 0

    fun catchHandler(err: dynamic): Promise<Any?> {
        if (!jsTruthy(err)) throw IllegalStateException("open failed with falsy error: $err")
        if (err.code != "EEXIST") throw err
        counter++
        currentFileBaseName = "$name-$counter"
        currentFile = if (!unslashedPath.isNullOrEmpty())
            safeJoinPath(directory, unslashedPath, currentFileBaseName + postfix)
        else
            safeJoinPath(directory, currentFileBaseName + postfix)
        return open(currentFile, "wx").catch(::catchHandler)
    }
    if (!unslashedPath.isNullOrEmpty())
        mkdir(safeJoinPath(directory, unslashedPath), recursive = true)

    //prom: T where T:Union<FileHandle,Promise<T>>
    var prom: Any? = open(currentFile, "wx").catch(::catchHandler)

    while (prom is Promise<*>) {
        prom = prom.await()
    }
    return Triple(currentFileBaseName, prom.unsafeCast<FileHandle>(), currentFile)
}

class FilePlanetStore(val directory: String, val metaStore: IPlanetMetaStore) : IPlanetStore {

    private val liveFilePath: String = safeJoinPath(directory, "live")

    private suspend fun basePathIsFile(basePath: String): Boolean {
        return try {
            stat(safeJoinPath(directory, basePath)).await().isFile()
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT")
                throw ex.unsafeCast<Throwable>()
            false
        }
    }

    override suspend fun isPlanetPath(path: String): Boolean {
        return if (path.endsWith(".planet")) basePathIsFile(path) else basePathIsFile("$path.planet")
    }

    override suspend fun internalPlanetIDFromPath(path: String): String {
        return (if (path.endsWith(".planet"))
            (path.split('.').dropLast(1).joinToString(".")) else path)
            .split('/', '\\').last()
    }

    private fun shortenPath(path: String): String =
        path.removeCommon(directory).let { if (it.startsWith('/') || it.startsWith('\\')) it.substring(1) else it }

    private suspend fun lookupPath(id: String): String? {
        return lookupPaths(listOf(id))[0]
    }

    private suspend fun lookupPaths(ids: List<String>): List<String?> {
        val pathMap: Map<String, String> = listPlanetFiles("./", true).associate {
            it.split('.').dropLast(1).joinToString(".").split('/', '\\').last() to
                    shortenPath(it)
        }
        return ids.map(pathMap::get)
    }

    private suspend fun getPath(id: String): String? {
        val path = metaStore.retrieveFilePath(id, this::basePathIsFile, this::lookupPath)
        return safeJoinPath(directory, path ?: return null)
    }

    override suspend fun add(planet: SPlanet.Template, path: String?): SPlanet {
        if (path != null && !pathIsWhitelisted(path)) throw RESTResponseCodeException(
            HttpStatusCode.NotFound
        )
        val (name: String, handle: FileHandle, filePath: String) = makeClosestFile(directory, planet.name, path)
        try {
            handle.writeFile(planet.lines.joinToString(EOL)).await()
        } finally {
            handle.close()
        }
        val resultPlanet = planet.withID(name)
        metaStore.setInfo(resultPlanet.info)
        metaStore.setFilePath(name, shortenPath(filePath))
        return resultPlanet
    }

    override suspend fun remove(planet: SPlanet): Boolean {
        val oldInfo: ServerPlanetInfo = getInfo(planet.id) ?: return false
        if (oldInfo == planet.info) {
            removeBlind(oldInfo.id)
            return true
        }
        return false
    }

    override suspend fun removeBlind(id: String) {
        metaStore.removeInfoByID(id)
        try {
            unlink(getPath(id) ?: return).await()
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
            unlink(getPath(id) ?: return oldPlanet).await()
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT")
                throw ex.unsafeCast<Throwable>()
        }
        return oldPlanet
    }

    override suspend fun update(planet: SPlanet) {
        val path = getPath(planet.id)
        val stat: Stats? = if (path == null) null
        else stat(path).await()

        if (stat?.isFile() == true) {
            val handle: FileHandle = open(path!!, "w").await()
            try {
                handle.writeFile(planet.planetFile.contentString).await()
            } finally {
                handle.close()
            }
        }

        metaStore.setInfo(planet.info, onlyIfExist = true)
    }

    override suspend fun get(id: String): SPlanet? {
        var planetFile: PlanetFile? = null
        if (id.contains('\u0000'))
            throw RESTResponseCodeException(
                HttpStatusCode.UnprocessableEntity,
                "Invalid id: \"${id.toIDString()}\""
            )
        val path: String = getPath(id) ?: return null
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
                return@retrieveInfo ServerPlanetInfo.fromPlanet(
                    id,
                    planetFile!!.planet,
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
        val info = metadata ?: ServerPlanetInfo.fromPlanet(
            id,
            planetFile!!.planet,
            stat(path).await().mtime.toDateTime(),
            nameOverride = name
        )
        return SPlanet(info, lines = planetFile!!.contentString.split("""\r?\n""".toRegex()))
    }

    override suspend fun getInfo(id: String): ServerPlanetInfo? {
        return metaStore.retrieveInfo(id, ::lookupInfo)
    }

    private suspend fun lookupInfo(id: String): ServerPlanetInfo? {
        if (id.contains('\u0000'))
            throw RESTResponseCodeException(
                HttpStatusCode.UnprocessableEntity,
                "Invalid id: \"${id.toIDString()}\""
            )
        return try {
            ServerPlanetInfo.fromPlanet(
                id,
                readPlanetFile(id)?.planet,
                stat(getPath(id) ?: return null).await().mtime.toDateTime()
            )
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT")
                throw ex.unsafeCast<Throwable>()
            else
                null
        }
    }

    private suspend fun readPlanetFile(id: String): PlanetFile? {
        val content = readFile(getPath(id) ?: return null).await()
        return PlanetFile(content)
    }

    private suspend fun readSanitizedDir(
        nestedPath: String,
        returnPaths: Boolean,
        checkFirstLevelWhitelist: Boolean
    ): List<String> {
        return readdirents(nestedPath).await().toList().flatMap {
            when {
                it.isDirectory() -> if ((!checkFirstLevelWhitelist) || it.name.matches(Config.Planets.firstSubdirectoryWhitelistRegex))
                    readSanitizedDir(
                        safeJoinPath(nestedPath, it.name),
                        returnPaths,
                        false
                    ) else emptyList()
                it.name.endsWith(".planet") -> listOf(
                    if (returnPaths) safeJoinPath(
                        nestedPath,
                        it.name
                    ) else it.name.split('.').dropLast(1).joinToString(".")
                )
                else -> emptyList()
            }
        }
    }

    companion object {
        fun pathIsWhitelisted(path: String): Boolean {
            val firstLevelPath = path.trimStart('.').trimStart('/', '\\').substringBefore('/').substringBefore('\\')
            //println("Matching \"$firstLevelPath\" of \"$path\" against ${Config.Planets.firstSubdirectoryWhitelistRegex}")
            if (!firstLevelPath.matches(Config.Planets.firstSubdirectoryWhitelistRegex)) return false
            //println("Match success")
            return true
        }
    }


    private suspend fun listPlanetFiles(path: String, returnPaths: Boolean): List<String> {
        if (!pathIsWhitelisted(path)) throw RESTResponseCodeException(
            HttpStatusCode.NotFound
        )
        val targetPath = safeJoinPath(directory, path)
        try {
            return readSanitizedDir(targetPath, returnPaths, path == "./" || path == "/" || path == "." || path == "")
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT" && ex.code != "ENOTDIR")
                throw ex.unsafeCast<Throwable>()
            else
                throw RESTResponseCodeException(HttpStatusCode.NotFound)
        }
    }

    override suspend fun listPlanets(path: String): List<ServerPlanetInfo> {
        return metaStore.retrieveInfo(
            listPlanetFiles(path, false),
            ::lookupInfo
        ).filterNotNull()
    }

    override suspend fun listFileEntries(path: String): DirectoryInfo.ServerContentInfo? {
        if (!pathIsWhitelisted(path)) throw RESTResponseCodeException(
            HttpStatusCode.NotFound
        )
        val safePath = safeJoinPath(directory, path).trimEnd('\\', '/')
        return try {
            val stats: Stats = stat(safePath).await()
            if (!stats.isDirectory())
                return null
            val (directories, files) = readdirents(safePath).await().toList().partition(Dirent::isDirectory)
            var relativeBasePath = safePath.removeCommon(directory).replace('\\', '/')
            if (!relativeBasePath.startsWith('/')) relativeBasePath = "/$relativeBasePath"
            DirectoryInfo.ServerContentInfo(
                relativeBasePath,
                stats.mtime.toDateTime(),
                directories.mapNotNull {
                    if (pathIsWhitelisted(pathJoin(path, it.name))) {
                        DirectoryInfo.MetaInfo(
                            if (relativeBasePath.endsWith("/")) relativeBasePath + it.name
                            else relativeBasePath + "/" + it.name,
                            stat(pathJoin(safePath, it.name)).await().mtime.toDateTime(),
                            readdirents(pathJoin(safePath, it.name)).await().toList().count { subEnt ->
                                //TODO: Cache count
                                subEnt.isDirectory() || (subEnt.isFile() && subEnt.name.endsWith(".planet"))
                            }
                        )
                    } else null
                },
                files.mapNotNull { getInfo(internalPlanetIDFromPath(pathJoin(safePath, it.name))) })
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT")
                throw ex.unsafeCast<Throwable>()
            else
                null
        }
    }

    override suspend fun listLivePlanets(path: String): List<ServerPlanetInfo> {
        val rawPaths: List<String> = try {
            readFile(liveFilePath).await().lines()
        } catch (ex: dynamic) {
            if (ex.code != "ENOENT")
                throw ex.unsafeCast<Throwable>()
            else {
                Logger.DEFAULT.warn { "Could not find the \"live\" file in \"$directory\", assuming it to be empty" }
                emptyList()
            }
        }
        return rawPaths.mapNotNull { getInfo(internalPlanetIDFromPath(it)) }
    }

    override suspend fun clearMeta(): Pair<Boolean, String> = metaStore.clear()

}