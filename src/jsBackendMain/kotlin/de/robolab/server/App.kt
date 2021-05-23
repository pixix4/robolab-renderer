package de.robolab.server

import de.robolab.common.auth.AccessLevel
import de.robolab.common.auth.User
import de.robolab.common.externaljs.NodeError
import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.externaljs.path.pathResolve
import de.robolab.common.utils.ConsoleGreeter
import de.robolab.common.utils.KeyValueStorage
import de.robolab.common.utils.Logger
import de.robolab.server.config.Config
import de.robolab.server.externaljs.cookie_parser.cookieParser
import de.robolab.server.externaljs.express.Request
import de.robolab.server.externaljs.express.Response
import de.robolab.server.net.DefaultEnvironment
import de.robolab.server.routes.logoResponse

fun main() {
    val args = js("process.argv.slice(2)") as Array<String>

    KeyValueStorage.overrideFiles = args.toList()
    Logger.level = Config.General.logLevel

    ConsoleGreeter.greetServer()
    val logger = Logger("MainApp")

    val endpoints = mutableListOf<Pair<String, String>>()
    DefaultEnvironment.app.use(cookieParser())

    DefaultEnvironment.app.use { req: Request<*>, res: Response<*>, next: (NodeError?) -> Unit ->
        req.user = User.Anonymous
        next(null)
    }

    DefaultEnvironment.app.use(Config.Api.mount, DefaultEnvironment.createApiRouter())
    endpoints += "api" to "http://localhost:${Config.General.port}${Config.Api.mount}"

    if (Config.Web.directory.isNotEmpty() && existsSync(Config.Web.directory)) {
        DefaultEnvironment.app.use(DefaultEnvironment.createWebRouter(Config.Web.mount, AccessLevel.Tutor))
        endpoints += "web" to "http://localhost:${Config.General.port}${Config.Web.mount} (${pathResolve(Config.Web.directory)})"
    }

    if (Config.Electron.directory.isNotEmpty() && existsSync(Config.Electron.directory)) {
        DefaultEnvironment.app.use(Config.Electron.mount, DefaultEnvironment.createElectronRouter())
        endpoints += "electron" to "http://localhost:${Config.General.port}${Config.Electron.mount} (${
            pathResolve(
                Config.Electron.directory
            )
        })"
    }

    if (Config.Api.mount.isEmpty() || Config.Api.mount != "/") {
        DefaultEnvironment.app.get("/", logoResponse)
    }

    Logger.level = Config.General.logLevel
    DefaultEnvironment.http.listen(Config.General.port) {
        logger.info {
            buildString {
                appendLine("Server successfully started!")
                append(" ".repeat(4))
                appendLine("Endpoints:")
                val length = endpoints.fold(0) { acc, (name, _) ->
                    kotlin.math.max(acc, name.length)
                } + 1
                for ((name, url) in endpoints) {
                    append(" ".repeat(8))
                    appendLine("${"$name:".padEnd(length)} $url")
                }
            }.trimEnd()
        }
    }
}
