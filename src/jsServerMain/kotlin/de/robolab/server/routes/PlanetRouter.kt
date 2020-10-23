@file:Suppress("UnsafeCastFromDynamic")

package de.robolab.server.routes

import de.robolab.client.app.model.base.SearchRequest
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.server.config.Config
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.utils.map
import de.robolab.server.auth.requireTutor
import de.robolab.server.net.RESTResponseCodeException
import de.robolab.server.data.*
import de.robolab.server.externaljs.*
import de.robolab.server.externaljs.express.*
import de.robolab.server.jsutils.*
import de.robolab.server.model.decodeID
import de.robolab.server.model.toIDString
import de.robolab.server.model.ServerPlanet as SPlanet

object PlanetRouter {
    val router: DefaultRouter = createRouter()
    val planetStore: IPlanetStore = FilePlanetStore(
        Config.Planets.directory,
        RedisPlanetMetaStore(Config.Planets.database, Config.Planets.connectionName)
    )
    val defaultIDRouter: PlanetIDRouter = PlanetIDRouter(planetStore)

    init {
        router.use("/:id", defaultIDRouter.baseRouter)
        router.getSuspend("/") { req, res ->
            val ignoreCase = jsTruthy(req.query["ignoreCase"])
            val infos: List<ServerPlanetInfo> = when {
                jsTruthy(req.query["query"]) -> planetStore.listPlanets(
                    SearchRequest.parse(req.query["query"].toString()),
                    ignoreCase
                )
                jsTruthy(req.query["name"]) -> planetStore.listPlanets(req.query["name"].toString(), ignoreCase)
                jsTruthy(req.query["nameContains"]) ||
                        jsTruthy(req.query["nameStartsWith"]) ||
                        jsTruthy(req.query["nameEndsWith"]) -> planetStore.listPlanets(
                    req.query["nameStartsWith"] as String?,
                    req.query["nameContains"] as String?,
                    req.query["nameEndsWith"] as String?,
                    ignoreCase
                )
                else -> planetStore.listPlanets()
            }
            res.status(HttpStatusCode.Ok)
            res.sendClientInfos(infos)
        }
        router.postSuspend("/") { req, res ->
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