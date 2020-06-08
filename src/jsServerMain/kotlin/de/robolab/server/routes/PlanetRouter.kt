package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.server.config.Config
import de.robolab.server.data.FilePlanetStore
import de.robolab.server.jsutils.promise
import de.robolab.common.planet.ID
import de.robolab.server.externaljs.*
import de.robolab.server.externaljs.express.*
import de.robolab.server.jsutils.jsTruthy
import de.robolab.server.model.ServerPlanet as SPlanet

object PlanetRouter {
    val router: Router = createRouter()

    private val planetStore: FilePlanetStore = FilePlanetStore(Config.Planets.directory)

    init {
        router.getPromise("/") { _, res ->
            promise {
                val ids: List<Pair<ID,String>> = planetStore.listPlanets()
                val strIDs: List<Pair<String,String>> = ids.map { it.first.id to it.second }
                res.status(HttpStatusCode.Ok)
                res.format("json" to {
                    res.send(strIDs.map {
                        val obj = emptyDynamic()
                        obj["id"] = it.first
                        obj["name"] = it.second
                        return@map obj
                    }.toJSArray())
                },"text" to {
                    res.send(strIDs.joinToString("\n") { "${it.first}:${it.second}" })
                })
            }
        }
        router.getPromise("/:id") { req, res ->
            val id: ID = ID(req.params.id as String)
            promise {
                val planet: SPlanet? = planetStore.get(id)
                if (planet == null)
                    res.status(HttpStatusCode.NotFound).send("Planet with id '${id.id}' could not be found")
                else {
                    res.status(HttpStatusCode.Ok).format("json" to {
                        res.send(planet.lines.toJSArray())
                    }, "text" to {
                        res.send(planet.lines.joinToString("\n"))
                    })
                }
            }
        }
        router.postPromise("/") { _, res ->
            promise {
                val id: ID? = planetStore.add(de.robolab.server.model.ServerPlanet())
                if (id == null) {
                    res.sendStatus(HttpStatusCode.NotFound)
                } else {
                    res.status(HttpStatusCode.Created).format("json" to {
                        res.send(JSON.stringify(id.id))
                    }, "text" to {
                        res.send(id.id)
                    })
                }
            }
        }
        router.putPromise("/:id") { req, res ->
            val id: ID = ID(req.params.id as String)
            promise {
                val planet: SPlanet? = planetStore.get(id)
                if (planet == null)
                    res.status(HttpStatusCode.NotFound).send("Planet with id '${id.id}' could not be found")
                else {
                    val planetLines: List<String>
                    if (req.isMimeType("json").jsTruthy()) {
                        val body: dynamic = req.body

                        @Suppress("UnsafeCastFromDynamic")
                        val dynJson: dynamic = if (body is String) JSON.parse(body) else body
                        @Suppress("USELESS_CAST")
                        if (dynJson is String) {
                            planetLines = dynJson.split("\n") as List<String>
                        } else if (isJSArray(dynJson as? Any))
                            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                            planetLines = (dynJson as JSArray<String>).toList()
                        else {
                            res.sendStatus(HttpStatusCode.UnprocessableEntity)
                            return@promise
                        }
                    } else if (req.isMimeType("text").jsTruthy()) {
                        val body: dynamic = req.body
                        if (body !is String) {
                            res.sendStatus(HttpStatusCode.BadRequest)
                            return@promise
                        }
                        planetLines = body.split("\n") as List<String>
                    } else {
                        res.sendStatus(HttpStatusCode.UnsupportedMediaType)
                        return@promise
                    }
                    planet.lockLines()
                    try {
                        planet.lines.clear()
                        planet.lines.addAll(planetLines)
                    } finally {
                        planet.unlockLines()
                    }
                    res.sendStatus(HttpStatusCode.NoContent)
                }
            }
        }
    }
}