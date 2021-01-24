@file:Suppress("UnsafeCastFromDynamic")

package de.robolab.server.routes

import NodeJS.NewListenerListener
import com.soywiz.klock.Time
import de.robolab.client.app.model.base.SearchRequest
import de.robolab.common.auth.*
import de.robolab.common.externaljs.JSArray
import de.robolab.common.externaljs.isJSArray
import de.robolab.common.externaljs.path.safeJoinPath
import de.robolab.common.externaljs.toList
import de.robolab.common.jsutils.jsTruthy
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.net.data.ServerDirectoryInfo
import de.robolab.server.config.Config
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.utils.Logger
import de.robolab.common.utils.map
import de.robolab.server.net.RESTResponseCodeException
import de.robolab.server.data.*
import de.robolab.server.externaljs.*
import de.robolab.server.externaljs.express.*
import de.robolab.server.jsutils.*
import de.robolab.server.model.decodeID
import de.robolab.server.model.toIDString
import io.ktor.util.*
import io.ktor.util.date.*
import path.path
import de.robolab.server.model.ServerPlanet as SPlanet

object PlanetRouter {
    val planetsRouter: DefaultRouter = createRouter()
    val planetStore: IPlanetStore = FilePlanetStore(
        path.resolve(Config.Planets.directory),
        RedisPlanetMetaStore(Config.Planets.database, Config.Planets.connectionName)
    )
    val idRouter: PlanetIDRouter = PlanetIDRouter(planetStore)
    private var lastPlanetsURLDeprecationWarning: Long = 0

    init {
        planetsRouter.getSuspend("*") { req, res ->
            val ignoreCase = jsTruthy(req.query["ignoreCase"])
            val sourceMode: String = (req.query["source"] as String?) ?: "flat"

            if (sourceMode != "live") {
                req.user.requireTutor()
            } else {
                req.user.requireGroupMember()
            }

            val path = ".${req.path}"

            if (path != "./" && path != "." && req.user.hasTutorAccess && planetStore.isPlanetPath(path)) {
                res.redirect("${req.baseUrl}/../planet/${planetStore.internalPlanetIDFromPath(path).toIDString()}")
                return@getSuspend
            }

            if ((!jsTruthy(req.query["source"])) && req.user.hasTutorAccess) //TODO: Remove alternative planet lookup
            {
                val planet = planetStore.get(req.path.trim('/', '\\').decodeID())
                if (planet != null) {
                    if (lastPlanetsURLDeprecationWarning + 150_000 < getTimeMillis()) {
                        lastPlanetsURLDeprecationWarning = getTimeMillis()
                        Logger.DEFAULT.warn {
                            "Deprecation: Accessing planets by ID under /api/planets will be removed soon, use /api/planet instead (no 's'); Attempted URL: \"${req.baseUrl}\" + \"${req.url}\""
                        }
                        res.setHeader(
                            "X-Deprecation-Warning",
                            "Deprecation: Accessing planets by ID under /api/planets will be removed soon, use /api/planet instead (no 's'); Attempted URL: \"${req.baseUrl}\" + \"${req.url}\""
                        )
                    }
                    res.status(HttpStatusCode.Ok).sendPlanet(planet)
                    return@getSuspend
                }
            }
            when (sourceMode) {
                "flat" -> {
                    val infos: List<ServerPlanetInfo> = when {
                        jsTruthy(req.query["query"]) -> planetStore.listPlanets(
                            path,
                            SearchRequest.parse(req.query["query"].toString()),
                            ignoreCase
                        )
                        jsTruthy(req.query["name"]) -> planetStore.listPlanets(
                            path,
                            req.query["name"].toString(),
                            ignoreCase
                        )
                        jsTruthy(req.query["nameContains"]) ||
                                jsTruthy(req.query["nameStartsWith"]) ||
                                jsTruthy(req.query["nameEndsWith"]) -> planetStore.listPlanets(
                            path,
                            req.query["nameStartsWith"] as String?,
                            req.query["nameContains"] as String?,
                            req.query["nameEndsWith"] as String?,
                            ignoreCase
                        )
                        else -> planetStore.listPlanets(path)
                    }
                    res.status(HttpStatusCode.Ok)
                    res.sendClientInfos(infos)
                }
                "live" -> {
                    val infos: List<ServerPlanetInfo> = when {
                        jsTruthy(req.query["query"]) -> planetStore.listLivePlanets(
                            path,
                            SearchRequest.parse(req.query["query"].toString()),
                            ignoreCase
                        )
                        jsTruthy(req.query["name"]) -> planetStore.listLivePlanets(
                            path,
                            req.query["name"].toString(),
                            ignoreCase
                        )
                        jsTruthy(req.query["nameContains"]) ||
                                jsTruthy(req.query["nameStartsWith"]) ||
                                jsTruthy(req.query["nameEndsWith"]) -> planetStore.listLivePlanets(
                            path,
                            req.query["nameStartsWith"] as String?,
                            req.query["nameContains"] as String?,
                            req.query["nameEndsWith"] as String?,
                            ignoreCase
                        )
                        else -> planetStore.listLivePlanets(path)
                    }
                    res.status(HttpStatusCode.Ok)
                    res.sendClientInfos(infos)
                }
                "nested" -> {
                    val info: ServerDirectoryInfo? = when {
                        jsTruthy(req.query["query"]) -> throw RESTResponseCodeException(
                            HttpStatusCode.BadRequest,
                            "Cannot use \"query\" together with \"nested\""
                        )
                        jsTruthy(req.query["name"])
                        -> planetStore.listFileEntries(
                            path,
                            req.query["name"].toString(),
                            ignoreCase
                        )
                        jsTruthy(req.query["nameContains"]) ||
                                jsTruthy(req.query["nameStartsWith"]) ||
                                jsTruthy(req.query["nameEndsWith"]) -> planetStore.listFileEntries(
                            path,
                            req.query["nameStartsWith"] as String?,
                            req.query["nameContains"] as String?,
                            req.query["nameEndsWith"] as String?,
                            ignoreCase
                        )
                        else -> planetStore.listFileEntries(path)
                    }
                    if (info == null) {
                        res.sendStatus(HttpStatusCode.NotFound)
                    } else {
                        res.status(HttpStatusCode.Ok)
                        res.sendDirectoryInfo(info)
                    }
                }
                else -> {
                    throw RESTResponseCodeException(
                        HttpStatusCode.BadRequest,
                        "Unknown source: \"$sourceMode\""
                    )
                }
            }
        }

        planetsRouter.postSuspend("*") { req, res ->
            req.user.requireTutor()
            val lines: List<String>? =
                if (req.body == null || req.body == undefined) null else when (req.mimeType) {
                    MIMEType.PlainText -> (req.body as? String)?.split("""\r?\n""".toRegex())
                    MIMEType.JSON -> req.body.unsafeCast<JSArray<dynamic>>().toList().filterIsInstance<String>()
                    else -> null
                }

            val template = if (lines != null) SPlanet.Template.fromLines(lines) else SPlanet.Template.random()
            val planet: SPlanet = planetStore.add(template, req.url)
            res.status(HttpStatusCode.Created).sendClientInfo(planet.info)
        }
    }

    fun mountOnRouter(router: DefaultRouter) {
        router.use("/planets", planetsRouter)
        router.postSuspend("/planet") { req, res ->
            req.user.requireTutor()
            val lines: List<String>? =
                if (req.body == null || req.body == undefined) null else when (req.mimeType) {
                    MIMEType.PlainText -> (req.body as? String)?.split("""\r?\n""".toRegex())
                    MIMEType.JSON -> req.body.unsafeCast<JSArray<dynamic>>().toList().filterIsInstance<String>()
                    else -> null
                }

            val template = if (lines != null) SPlanet.Template.fromLines(lines) else SPlanet.Template.random()
            val planet: SPlanet = planetStore.add(template)
            res.status(HttpStatusCode.Created).sendClientInfo(planet.info)
        }
        router.use("/planet/:id", idRouter.baseRouter)
    }

    suspend fun clearMeta(): Pair<Boolean, String> = planetStore.clearMeta()

    class PlanetIDRouter(planetStore: IPlanetStore) {
        val baseRouter: DefaultRouter = createRouter()
        val planetRouter: Router<PlanetRequestData, DefaultResponseData> =
            this.baseRouter.allMapRequestSuspend(PlanetRequestData.Factory(planetStore)::create)

        init {
            planetRouter.getSuspend("/") { req, res ->
                val planet = req.localData.assertPlanetFound()
                res.sendPlanet(planet)
            }

            planetRouter.getSuspend("/png") { req, res ->
                var scale: Double? = null
                if (req.query.scale) {
                    val numberString = req.query.scale.toString()
                    val number = numberString.toDoubleOrNull()
                    if (number != null) {
                        scale = number
                    } else {
                        throw IllegalArgumentException("'$numberString' is not a valid number!")
                    }
                }

                val planet = req.localData.assertPlanetFound()
                ExportRouter.exportPlanetAsPng(planet.planetFile, scale, res)
            }

            planetRouter.getSuspend("/svg") { req, res ->
                val planet = req.localData.assertPlanetFound()
                ExportRouter.exportPlanetAsSvg(planet.planetFile, res)
            }

            planetRouter.putSuspend("/") { req, res ->
                req.user.requireTutor()
                val planet: SPlanet = req.localData.assertPlanetFound()
                val planetContent: String =
                    when (req.mimeType) {
                        null -> ""
                        MIMEType.JSON -> {
                            val body: dynamic = req.body

                            val dynJson: dynamic = if (body is String) JSON.parse(body) else body
                            @Suppress("USELESS_CAST")
                            when {
                                dynJson is String -> dynJson
                                isJSArray(dynJson as? Any) -> dynJson.unsafeCast<JSArray<String>>().join("\n")
                                else -> {
                                    res.sendStatus(HttpStatusCode.UnprocessableEntity)
                                    return@putSuspend
                                }
                            }
                        }
                        MIMEType.PlainText -> {
                            val body: Any? = req.body
                            if (body !is String) {
                                res.sendStatus(HttpStatusCode.BadRequest)
                                return@putSuspend
                            }
                            body
                        }
                        else -> {
                            res.sendStatus(HttpStatusCode.UnsupportedMediaType)
                            return@putSuspend
                        }
                    }
                planet.lockLines()
                try {
                    planet.planetFile.replaceContent(planetContent)
                } finally {
                    planet.unlockLines()
                }
                PlanetRouter.planetStore.update(planet)
                res.status(HttpStatusCode.Ok)
                res.sendClientInfo(planet.info)
            }

            planetRouter.deleteSuspend("/") { req, res ->
                req.user.requireTutor()
                val id = req.localData.requestedID
                val removedPlanet = planetStore.remove(id) ?: req.localData.throwIDNotFound()
                res.sendPlanet(removedPlanet)
            }
        }

        class PlanetRequestData private constructor(
            val requestedID: String,
            val planetInfo: ServerPlanetInfo?,
            val planet: SPlanet?
        ) {

            fun throwIDNotFound(): Nothing {
                throw RESTResponseCodeException(
                    HttpStatusCode.NotFound,
                    "Planet with id '${requestedID.toIDString()}' could not be found"
                )
            }

            fun assertPlanetFound(): SPlanet {
                return planet ?: throwIDNotFound()
            }

            internal class Factory(
                private val planetStore: IPlanetStore,
                private val assertAccess: suspend (Request<*>, id: String, ServerPlanetInfo?) -> Unit = { _, _, _ -> }
            ) {
                suspend fun create(req: Request<*>): PlanetRequestData {
                    val id by req.paramProp.map(String::decodeID)
                    val planetInfo = this.planetStore.getInfo(id)
                    assertAccess(req, id, planetInfo)
                    val planet = planetStore.get(planetInfo)
                    return PlanetRequestData(
                        requestedID = id,
                        planetInfo = planetInfo,
                        planet = planet
                    )
                }
            }
        }
    }
}