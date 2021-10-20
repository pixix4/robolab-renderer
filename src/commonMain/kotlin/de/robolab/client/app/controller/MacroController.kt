package de.robolab.client.app.controller

import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.events.KeyEvent
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.DummyReplOutput
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.commands.macro.MacroCommand
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Logger
import de.robolab.common.utils.RobolabJson
import de.robolab.common.utils.autoLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class MacroController {

    @Serializable
    data class KeyBinding(
        val keyCode: KeyCode,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false,
    ) : IReplCommandParameter {

        override fun toToken(): String = toString()

        override fun toString(): String {
            return buildString {
                if (ctrlKey) {
                    append("Ctrl+")
                }
                if (shiftKey) {
                    append("Shift+")
                }
                if (altKey) {
                    append("Alt+")
                }
                append(keyCode.name)
            }
        }

        companion object : IReplCommandParameterTypeDescriptor<KeyBinding> {
            override val klazz: KClass<KeyBinding> = KeyBinding::class
            override val name: String = "KeyBinding"
            override val description = "Describes a bindable key combination"
            override val pattern = "[Ctrl+][Shift+][Alt+]<KeyCode>"
            override val example = listOf("Ctrl+K")
            override val regex: Regex = """(\w+\+)*\w+""".toRegex()

            override fun fromToken(token: String, match: MatchResult): KeyBinding? {
                val split = token.split('+')

                val modifier = split.dropLast(1).map { it.lowercase().trim() }
                val keyCode = KeyCode.values().find { it.name.equals(split.last().trim(), true) } ?: return null

                return KeyBinding(
                    keyCode,
                    ctrlKey = modifier.any { it == "ctrl" },
                    shiftKey = modifier.any { it == "shift" },
                    altKey = modifier.any { it == "alt" },
                )
            }

            fun fromKeyEvent(event: KeyEvent) = KeyBinding(
                event.keyCode,
                event.ctrlKey,
                event.altKey,
                event.shiftKey
            )
        }
    }

    @Serializable
    data class Macro(
        val keyBinding: KeyBinding,
        val commands: List<String>,
    ) {

        fun withAlias(vararg alias: KeyBinding): List<Macro> {
            return listOf(this) + alias.map { binding ->
                Macro(binding, listOf(
                    "macro execute ${this.keyBinding.toToken()}"
                ))
            }
        }
    }

    @Serializable
    data class MacroStorage(
        val macroList: List<Macro>,
    )

    val macroList = mutableListOf<Macro>()
    var debugOutput: IReplOutput? = null

    fun onKeyDown(event: KeyEvent) {
        debugOutput?.writeln(event.toString())
        if (execute(KeyBinding.fromKeyEvent(event), debugOutput ?: DummyReplOutput)) {
            event.stopPropagation()
        }
    }

    fun execute(binding: KeyBinding, output: IReplOutput): Boolean {
        val macro = macroList.find { it.keyBinding == binding } ?: return false

        GlobalScope.launch {
            for (line in macro.commands) {
                ReplExecutor.execute(line, output)
            }
        }
        return true
    }

    fun save() {
        PreferenceStorage.macros = RobolabJson.encodeToString(MacroStorage.serializer(), MacroStorage(macroList))
    }

    fun load() {
        val macros = PreferenceStorage.macros

        try {
            val storage = RobolabJson.decodeFromString(MacroStorage.serializer(), macros)

            macroList.clear()
            macroList.addAll(storage.macroList)
        } catch (e: Exception) {
            autoLogger.debug("Exception during macro-loading",e)
            loadDefaults()
            save()
        }
    }

    fun loadDefaults() {
        macroList.clear()

        macroList.addAll(Macro(
            KeyBinding(KeyCode.SLASH, ctrlKey = true),
            listOf("window toggle terminal")
        ).withAlias(KeyBinding(KeyCode.ENTER, ctrlKey = true), KeyBinding(KeyCode.HASH, ctrlKey = true)))
    }

    init {
        load()

        MacroCommand.bind(this)
    }
}
