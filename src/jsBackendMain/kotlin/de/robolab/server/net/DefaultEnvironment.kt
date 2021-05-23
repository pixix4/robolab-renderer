package de.robolab.server.net

import de.robolab.common.auth.AccessLevel
import de.robolab.common.auth.User
import de.robolab.common.auth.requireAccessEnabled
import de.robolab.common.externaljs.emptyDynamic
import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.externaljs.fs.readdirSync
import de.robolab.common.externaljs.http.createServer
import de.robolab.common.externaljs.path.pathJoin
import de.robolab.common.externaljs.path.pathResolve
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AccessControlAllowMethods
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Version
import de.robolab.server.config.Config
import de.robolab.server.externaljs.body_parser.json
import de.robolab.server.externaljs.body_parser.text
import de.robolab.server.externaljs.createIO
import de.robolab.server.externaljs.express.*
import de.robolab.server.jsutils.setHeader
import de.robolab.server.routes.*
import kotlinx.serialization.Serializable

object DefaultEnvironment {
    val app: ExpressApp = createApp()
    val http: dynamic = createServer(app)
    val io: dynamic = createIO(http)

    val logger = Logger("DefaultEnvironment")

    fun createApiRouter(): DefaultRouter {
        val router = createRouter()
        router.use("/") { req, res, next ->
            res.setHeader(AccessControlAllowMethods.All)
            res.setHeader("Access-Control-Allow-Origin", req.headers.Origin ?: req.headers.origin ?: "*")
            res.setHeader("Access-Control-Allow-Headers", "authorization")
            res.setHeader("Access-Control-Allow-Credentials", true)
            res.setHeader("Access-Control-Max-Age", 3600)
            res.setHeader("Vary", "Origin")
            next(null)
        }
        router.use(json())
        router.use(text())
        //router.use(cookieParser())
        //moved to App.kt
        router.use(fileUpload())
        //router.use(AuthRouter::userLookupMiddleware)
        //moved to App.kt
        router.use("/tea", BeverageRouter.teaRouter)
        router.use("/coffee", BeverageRouter.coffeeRouter)
        router.use("/mate", BeverageRouter.mateRouter)
        if (existsSync(pathResolve(Config.Planets.directory))) {
            PlanetRouter.mountOnRouter(router) // mounts "/planet" and "/planets"
        } else {
            logger.warn { "Cannot find planet directory '${pathResolve(Config.Planets.directory)}'" }
        }
        router.use("/info", InfoRouter.router)
        router.use("/mqtt", MQTTRouter.router)
        router.use("/export", ExportRouter.router)

        router.get("/version") { _, res ->
            res.status(HttpStatusCode.Ok)
            res.format("json" to {
                val obj = emptyDynamic()
                obj["version"] = BuildInformation.versionBackend
                obj["versionString"] = BuildInformation.versionBackend.toString()
                res.send(JSON.stringify(obj))
            }, "text" to {
                res.send(BuildInformation.versionBackend.toString())
            })
        }

        router.get("/", logoResponse)

        return router
    }

    fun createWebRouter(mount: String, minAccessLevel: AccessLevel = AccessLevel.Anonymous): DefaultRouter {
        val directory = pathResolve(Config.Web.directory)
        val router = createRouter(strict = true)

        val rawMount: String
        val slashMount: String

        if (mount.endsWith("/")) {
            slashMount = mount
            rawMount = mount.substring(0, mount.length - 1)
        } else {
            rawMount = mount
            slashMount = "$mount/"
        }
        if (minAccessLevel != AccessLevel.Anonymous && requireAccessEnabled) {
            router.use(slashMount) { req, res, next ->
                if ((req.path != "/") && (req.path != "index.html") && (req.path != "/index.html")) {
                    next(null)
                    return@use
                }
                if (req.user == User.Anonymous) {
                    if (Config.Web.mount.endsWith("/"))
                        res.redirect(Config.Web.mount + "api/auth/gitlab")
                    else
                        res.redirect(Config.Web.mount + "/api/auth/gitlab")
                } else if (req.user.canAccess(minAccessLevel)) {
                    next(null)
                } else {
                    res.sendStatus(HttpStatusCode.Forbidden)
                }
            }
        }
        router.get(slashMount) { _, res -> res.sendFile(pathJoin(directory, "index.html")) }
        router.get(rawMount) { _, res -> res.redirect(301, slashMount) }

        router.use(rawMount, createStatic(directory))

        return router
    }

    fun createElectronRouter(): DefaultRouter {
        val mount = Config.Electron.mount
        val directory = pathResolve(Config.Electron.directory)
        val router = createRouter()

        router.get("/latest.json") { _, res ->
            res.setHeader("content-type", "application/json")

            val content = readdirSync(directory).toList().filterNot {
                it.contains(".yml")
            }.filter {
                it.contains(".")
            }

            val artefacts = content.mapNotNull {
                Artefact.parse(mount, it)
            }.groupBy { Triple(it.os, it.arch, it.format) }.mapValues { (_, l) ->
                l.sortedByDescending { Version.parse(it.version) }.take(1)
            }.values.flatten()

            val data = artefacts.groupBy {
                it.os
            }.mapValues { (_, v) ->
                v.groupBy { it.arch }
            }

            res.sendSerializable(data)
        }
        router.use("", createStatic(directory))

        return router
    }
}

@Serializable
data class Artefact(
    val url: String,
    val filename: String,
    val os: String,
    val arch: String,
    val format: String,
    val version: String,
) {
    companion object {

        fun parse(mount: String, name: String): Artefact? {
            val url = "$mount/$name".replace("//+".toRegex(), "/")

            var n = name.replace("robolab-renderer([-_])".toRegex(), "")
            if (n.endsWith(".dmg")) {
                n = n.removeSuffix(".dmg")

                return when {
                    n.endsWith("-arm64") -> {
                        n = n.removeSuffix("-arm64")

                        Artefact(
                            url,
                            name,
                            "mac",
                            "arm64",
                            "dmg",
                            n
                        )
                    }
                    n.endsWith("-universal") -> {
                        n = n.removeSuffix("-universal")

                        Artefact(
                            url,
                            name,
                            "mac",
                            "universal",
                            "dmg",
                            n
                        )
                    }
                    else -> {
                        Artefact(
                            url,
                            name,
                            "mac",
                            "x86",
                            "dmg",
                            n
                        )
                    }
                }
            }

            if (n.endsWith("-mac.zip")) {
                n = n.removeSuffix("-mac.zip")

                return when {
                    n.endsWith("-arm64") -> {
                        n = n.removeSuffix("-arm64")

                        Artefact(
                            url,
                            name,
                            "mac",
                            "arm64",
                            "zip",
                            n
                        )
                    }
                    n.endsWith("-universal") -> {
                        n = n.removeSuffix("-universal")

                        Artefact(
                            url,
                            name,
                            "mac",
                            "universal",
                            "zip",
                            n
                        )
                    }
                    else -> {
                        Artefact(
                            url,
                            name,
                            "mac",
                            "x86",
                            "zip",
                            n
                        )
                    }
                }
            }

            if (n.endsWith(".AppImage")) {
                n = n.removeSuffix(".AppImage")

                return if (n.endsWith("-arm64")) {
                    n = n.removeSuffix("-arm64")

                    Artefact(
                        url,
                        name,
                        "linux",
                        "arm64",
                        "AppImage",
                        n
                    )
                } else {
                    Artefact(
                        url,
                        name,
                        "linux",
                        "x86",
                        "AppImage",
                        n
                    )
                }
            }

            if (n.endsWith(".deb")) {
                n = n.removeSuffix(".deb")

                return if (n.endsWith("_arm64")) {
                    n = n.removeSuffix("_arm64")

                    Artefact(
                        url,
                        name,
                        "linux",
                        "arm64",
                        "deb",
                        n
                    )
                } else {
                    Artefact(
                        url,
                        name,
                        "linux",
                        "x86",
                        "deb",
                        n
                    )
                }
            }

            if (n.endsWith(".exe")) {
                n = n.removeSuffix(".exe")

                return Artefact(
                    url,
                    name,
                    "windows",
                    "x86",
                    "exe",
                    n
                )
            }

            return null
        }
    }
}
