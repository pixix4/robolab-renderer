@file:Suppress("UnsafeCastFromDynamic")

package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.server.config.Config
import de.robolab.server.data.FilePlanetStore
import de.robolab.server.jsutils.promise
import de.robolab.common.planet.ID
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.server.data.RedisPlanetMetaStore
import de.robolab.server.data.listPlanets
import de.robolab.server.externaljs.*
import de.robolab.server.externaljs.express.*
import de.robolab.server.jsutils.jsTruthy
import de.robolab.server.model.decodeID
import de.robolab.server.model.toIDString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import de.robolab.server.model.ServerPlanet as SPlanet

object PlanetRouter {
    val router: Router = createRouter()

    private val planetStore: FilePlanetStore = FilePlanetStore(
        Config.Planets.directory,
        RedisPlanetMetaStore(Config.Planets.database)
    )

    init {
        router.getPromise("/") { req, res ->
            promise {
                val ignoreCase = jsTruthy(req.query["ignoreCase"])
                val infos: List<ServerPlanetInfo> = when {
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
                val encodedInfo: List<Pair<String, String>> = infos.map { it.id.toIDString() to it.name }
                res.status(HttpStatusCode.Ok)
                res.format("json" to {
                    res.send(encodedInfo.map {
                        val obj = emptyDynamic()
                        obj["id"] = it.first
                        obj["name"] = it.second
                        return@map obj
                    }.toJSArray())
                }, "text" to {
                    res.send(encodedInfo.joinToString("\n") { "${it.first}:${it.second}" })
                })
            }
        }
        router.getPromise("/:id") { req, res ->
            val id = (req.params.id as String).decodeID()
            promise {
                val planet: SPlanet? = planetStore.get(id)
                if (planet == null)
                    res.status(HttpStatusCode.NotFound).send("Planet with id '${id.toIDString()}' could not be found")
                else {
                    res.status(HttpStatusCode.Ok).format("json" to {
                        res.send(planet.lines.toJSArray())
                    }, "text" to {
                        res.send(planet.lines.joinToString("\n"))
                    })
                }
            }
        }
        router.postPromise("/") { req, res ->
            promise {
                val lines: List<String>? =
                    if (req.body == null || req.body == undefined) null else when (req.mimeType) {
                        MIMEType.PlainText -> (req.body as? String)?.split("""\r?\n""".toRegex())
                        MIMEType.JSON -> req.body.unsafeCast<JSArray<dynamic>>().toList().filterIsInstance<String>()
                        else -> null
                    }

                val template = if (lines != null) SPlanet.Template.fromLines(lines) else SPlanet.Template.random()
                val planet: SPlanet = planetStore.add(template)
                res.status(HttpStatusCode.Created).format("json" to {
                    res.send(JSON.stringify(planet.id.toIDString()))
                }, "text" to {
                    res.send(planet.id.toIDString())
                })
            }
        }
        router.putPromise("/:id") { req, res ->
            val id: String = (req.params.id as String).decodeID()
            promise {
                val planet: SPlanet? = planetStore.get(id)
                if (planet == null)
                    res.status(HttpStatusCode.NotFound).send("Planet with id '$id' could not be found")
                else {
                    val planetLines: List<String>
                    when (req.mimeType) {
                        null -> planetLines = emptyList()
                        MIMEType.JSON -> {
                            val body: dynamic = req.body

                            @Suppress("UnsafeCastFromDynamic")
                            val dynJson: dynamic = if (body is String) JSON.parse(body) else body
                            @Suppress("USELESS_CAST")
                            if (dynJson is String) {
                                planetLines = dynJson.split("""\r?\n""".toRegex()) as List<String>
                            } else if (isJSArray(dynJson as? Any))
                                @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                                planetLines = (dynJson as JSArray<String>).toList()
                            else {
                                res.sendStatus(HttpStatusCode.UnprocessableEntity)
                                return@promise
                            }
                        }
                        MIMEType.PlainText -> {
                            val body: Any? = req.body
                            if (body !is String) {
                                res.sendStatus(HttpStatusCode.BadRequest)
                                return@promise
                            }
                            planetLines = body.split("""\r?\n""".toRegex())
                        }
                        else -> {
                            res.sendStatus(HttpStatusCode.UnsupportedMediaType)
                            return@promise
                        }
                    }
                    planet.lockLines()
                    try {
                        planet.lines.clear()
                        planet.lines.addAll(planetLines)
                    } finally {
                        planet.unlockLines()
                    }
                    planetStore.update(planet.reparsed())
                    res.sendStatus(HttpStatusCode.NoContent)
                }
            }
        }
    }
}