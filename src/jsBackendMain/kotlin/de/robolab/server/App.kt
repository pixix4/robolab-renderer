package de.robolab.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.utils.ConsoleGreeter
import de.robolab.common.utils.KeyValueStorage
import de.robolab.common.utils.Logger
import de.robolab.server.config.Config
import de.robolab.server.net.DefaultEnvironment
import de.robolab.server.routes.logoResponse
import path.path

class App : CliktCommand() {

    private val configFiles by option("-c", "--config")
        .multiple()

    override fun run() {
        KeyValueStorage.overrideFiles = configFiles
        Logger.level = Config.General.logLevel

        ConsoleGreeter.greetServer()
        val logger = Logger("MainApp")

        val endpoints = mutableListOf<Pair<String, String>>()

        DefaultEnvironment.app.use(Config.Api.mount, DefaultEnvironment.createApiRouter())
        endpoints += "api" to "http://localhost:${Config.General.port}${Config.Api.mount}"

        if (Config.Web.directory.isNotEmpty() && existsSync(Config.Web.directory)) {
            DefaultEnvironment.app.use(Config.Web.mount, DefaultEnvironment.createWebRouter())
            endpoints += "web" to "http://localhost:${Config.General.port}${Config.Web.mount} (${path.resolve(Config.Web.directory)})"
        }

        if (Config.Electron.directory.isNotEmpty() && existsSync(Config.Electron.directory)) {
            DefaultEnvironment.app.use(Config.Electron.mount, DefaultEnvironment.createElectronRouter())
            endpoints += "electron" to "http://localhost:${Config.General.port}${Config.Electron.mount} (${path.resolve(Config.Electron.directory)})"
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
}

fun main() {
    val args = js("process.argv.slice(2)") as Array<String>
    App().main(args)
}
