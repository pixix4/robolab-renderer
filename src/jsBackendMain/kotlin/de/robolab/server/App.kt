package de.robolab.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import de.robolab.common.utils.ConsoleGreeter
import de.robolab.common.utils.KeyValueStorage
import de.robolab.common.utils.Logger
import de.robolab.server.config.Config
import de.robolab.server.net.DefaultEnvironment
import de.robolab.server.routes.logoResponse

class App : CliktCommand() {

    val configFiles by option("-c", "--config")
        .multiple()

    override fun run() {
        KeyValueStorage.overrideFiles = configFiles

        ConsoleGreeter.greetServer()
        val logger = Logger("MainApp")

        DefaultEnvironment.app.use(Config.General.mount, DefaultEnvironment.createApiRouter())
        if (Config.General.mount.isEmpty() || Config.General.mount != "/") {
            DefaultEnvironment.app.get("/", logoResponse)
        }
        DefaultEnvironment.http.listen(Config.General.port) {
            logger.info {
                "Listening on port http://localhost:${Config.General.port}${Config.General.mount}"
            }
        }
    }
}

fun main() {
    val args = js("process.argv.slice(2)") as Array<String>
    App().main(args)
}
