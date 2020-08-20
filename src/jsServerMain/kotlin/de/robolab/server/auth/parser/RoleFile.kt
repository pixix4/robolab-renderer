package de.robolab.server.auth.parser

import de.robolab.server.auth.AuthManager
import de.robolab.server.auth.IAuthProvider
import de.robolab.server.auth.ProviderFactory
import de.robolab.server.auth.parser.RoleFileInstruction.Companion.replaceParamPlaceholders
import de.robolab.server.externaljs.NodeErrors
import de.robolab.server.externaljs.fs.copyFile
import de.robolab.server.externaljs.fs.copyFileSync
import de.robolab.server.externaljs.fs.readFile
import de.robolab.server.externaljs.fs.readFileSync
import kotlinx.coroutines.await

class RoleFile(val content: List<RoleFileInstruction>) {

    class RoleFileBuilder {

        private val _content: MutableList<RoleFileInstruction> = mutableListOf()
        val content: List<RoleFileInstruction> = _content

        operator fun RoleFileInstruction.unaryPlus() {
            this@RoleFileBuilder.addRaw(this)
        }

        operator fun String.unaryPlus() {
            this@RoleFileBuilder.addRaw(this)
        }

        fun addRaw(instruction: RoleFileInstruction) {
            _content.add(instruction)
        }

        fun addRaw(instruction: String) = addRaw(RoleFileInstruction.parse(instruction))

        fun buildFile(): RoleFile = RoleFile(content)

        fun buildMap(): PermissionMap = buildFile().buildMap()

        fun buildManager(): AuthManager = buildFile().buildManager()
    }

    constructor(block: RoleFileBuilder.() -> Unit) : this(RoleFileBuilder().apply(block).content)

    fun buildMap(): PermissionMap {
        val map = PermissionMap()
        var isGlobalClass: Boolean = true
        var currentClass: RoleFileInstruction.ClassDeclaration? = null
        var currentProvider: RoleFileInstruction.ProviderDeclaration? = null
        for ((index, line) in content.withIndex()) {
            if (line !is RoleFileInstruction.PermissionInstruction && line !is RoleFileInstruction.Comment){
                isGlobalClass = false
                currentClass = null
            }
            if (line !is RoleFileInstruction.ProviderPermission && line !is RoleFileInstruction.Comment)
                currentProvider = null

            when (line) {
                is RoleFileInstruction.PermissionInstruction.LiteralPermissionInstruction ->
                    if (currentClass == null) {
                        if (isGlobalClass) map.addPermission(setOf(line))
                        else throw IllegalStateException("Global permissions must be placed at the top of the file: Line ${index + 1}: $line")
                    } else map.addPermission(currentClass, setOf(line))
                is RoleFileInstruction.PermissionInstruction.ClassPermissionInstruction -> {; }
                is RoleFileInstruction.Comment -> {; }
                is RoleFileInstruction.ClassDeclaration -> {
                    currentClass = line
                }
                is RoleFileInstruction.ProviderDeclaration -> {
                    currentProvider = line
                }
                is RoleFileInstruction.ProviderPermission -> {
                    map.addProvider(
                        currentProvider
                            ?: throw IllegalStateException("Could not find provider declaration for $line (Line: ${index + 1})"),
                        line
                    )
                }
            }
        }
        if (currentClass != null)
            map.addPermission(currentClass, emptySet())
        if (currentProvider != null)
            map.addProvider(currentProvider)
        return map
    }

    fun buildManager(): AuthManager {
        val map = buildMap()
        return AuthManager(map.providers.entries.map { (decl, perms) ->
            ProviderFactory.create(decl, perms.toList(), map)
        })
    }

    companion object {
        fun parse(content: String): RoleFile = RoleFile(
            content.split('\n').map(String::trim).filter(String::isNotBlank).map(RoleFileInstruction.Parser::parse)
        )

        fun parseFile(location: String): RoleFile = parse(readFileSync(location, "utf8") as String)
        suspend fun parseFileSuspend(location: String): RoleFile = parse(readFile(location, "utf8").await())

        fun parseOrCopyFile(location: String, fallbackLocation: String = "./auth/roles.default.txt"): RoleFile {
            try {
                return parseFile(location)
            } catch (ex: dynamic) {
                NodeErrors.NoEntry.assertInstance(ex)
            }
            copyFileSync(fallbackLocation, location)
            return parseFile(location)
        }

        suspend fun parseOrCopyFileSuspend(
            location: String,
            fallbackLocation: String = "./auth/roles.default.txt"
        ): RoleFile {
            try {
                return parseFileSuspend(location)
            } catch (ex: dynamic) {
                NodeErrors.NoEntry.assertInstance(ex)
            }
            copyFile(fallbackLocation, location).await()
            return parseFileSuspend(location)
        }
    }
}