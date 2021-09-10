package de.robolab.server

import de.robolab.client.repl.*
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

data class LayoutConstraint(
    val rows: Int,
    val cols: Int,
) : IReplCommandParameter {

    override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion

    override fun toToken(): String = "${rows}x$cols"

    companion object : IReplCommandParameterTypeDescriptor<LayoutConstraint> {
        override val klazz: KClass<LayoutConstraint> = LayoutConstraint::class
        override val name: String = "LayoutConstraint"
        override val description = "Specify the window layout"
        override val pattern = "<rows>x<cols>"
        override val example = listOf(
            LayoutConstraint(3, 2).toToken()
        )
        override val regex: Regex = """\d+x\d+""".toRegex()

        override fun fromToken(token: String): LayoutConstraint? {
            val (cols, rows) = token.split("x", limit = 2)
            return LayoutConstraint(
                cols.toIntOrNull() ?: return null,
                rows.toIntOrNull() ?: return null
            )
        }
    }
}

suspend fun execute(command: String) {
    println("\n> $command")
    println("#".repeat(20))
    println(ReplExecutor.execute(command).joinToString("\n"))
    println("#".repeat(20))
}

fun autoComplete(command: String) {
    for (i in 0..command.length) {
        val subCommand = command.take(i)

        println("\n> $subCommand")
        println(ReplExecutor.autoComplete(subCommand).joinToString("\n").prependIndent("    "))
    }
}

fun hint(command: String, expand: Boolean = true) {
    val range = if (expand) 0..command.length else command.length..command.length
    for (i in range) {
        val subCommand = command.take(i)
        val hint = ReplExecutor.hint(subCommand)

        val grey = "\u001b[37m"
        val cyan = "\u001b[36m"
        val blue = "\u001b[34m"
        val green = "\u001b[32m"
        val red = "\u001b[31m"
        val reset = "\u001b[0m"

        val builder = StringBuilder()

        var lastSplit = 0

        for ((range, color) in hint.highlight) {
            builder.append(subCommand.substring(lastSplit, range.first))

            builder.append(when (color) {
                ReplExecutor.HintColor.NODE -> blue
                ReplExecutor.HintColor.LEAF -> cyan
                ReplExecutor.HintColor.PARAMETER -> green
                ReplExecutor.HintColor.ERROR -> red
            })
            builder.append(subCommand.substring(range))
            builder.append(reset)
            lastSplit = range.last + 1
        }
        builder.append(subCommand.substring(lastSplit, subCommand.length))

        println("\n> ${builder.toString()}$grey${hint.suffix}$reset")
    }
}

fun main() {
    val args = js("process.argv.slice(2)") as Array<String>

    KeyValueStorage.overrideFiles = args.toList()
    Logger.level = Config.General.logLevel

    ConsoleGreeter.greetServer()
    val logger = Logger("MainApp")

    val windowCommand = ReplCommandNode("window", "Modify the current window state")
    ReplRootCommand += windowCommand
    windowCommand += ReplSimpleCommand("split-h", "Split the current window horizontally") {
        listOf("Split-h")
    }
    windowCommand += ReplSimpleCommand("split-v", "Split the current window vertically") {
        listOf("Split-v")
    }
    windowCommand += ReplParameterCommand(
        "layout",
        "Transform the current window to the specified layout",
        ReplCommandParameterDescriptor(
            LayoutConstraint,
            "constraint",
            false
        ),
        ReplCommandParameterDescriptor(
            LayoutConstraint,
            "param2",
        true
        )
    ) { parameters ->
        val layoutConstraint = parameters.first() as LayoutConstraint

        listOf(
            "Set layout to $layoutConstraint"
        )
    }

    GlobalScope.launch {
        execute("help")
        execute("win")
        execute("window")
        execute("window help")
        execute("window split-v help")
        execute("window split-v")
        execute("window layout help")
        execute("window layout")
        execute("window layout 3x2")
        execute("window layout abc")

        autoComplete("window split-v")
        hint("window split-v")
        hint("window layout 3x2 4x5")
        hint("window layout asdf", false)
        hint("window layout 1234 asdf asdf", false)
        hint("window layout 2x2 asdf asdf", false)
        hint("window layout 2x2 2x2 asdf", false)
        hint("window asdf 1234", false)
    }

    return


    val endpoints = mutableListOf<Pair<String, String>>()
    DefaultEnvironment.app.use(cookieParser())

    DefaultEnvironment.app.use { req: Request<*>, res: Response<*>, next: (NodeError?) -> Unit ->
        val robolabAccess = req.get("X-RL-Access") ?: ""
        val robolabName = req.get("X-RL-Name") ?: ""
        val robolabId = req.get("X-RL-ID")?.toUIntOrNull() ?: UInt.MAX_VALUE
        val robolabGroup = req.get("X-RL-Group")?.toIntOrNull()

        req.user = when (robolabAccess) {
            "T" -> User(
                userID = robolabId,
                accessLevel = AccessLevel.Tutor,
                username = robolabName,
                group = robolabGroup,
            )
            "S" -> User(
                userID = robolabId,
                accessLevel = AccessLevel.GroupMember,
                username = robolabName,
                group = robolabGroup,
            )
            else -> User.Anonymous
        }

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
