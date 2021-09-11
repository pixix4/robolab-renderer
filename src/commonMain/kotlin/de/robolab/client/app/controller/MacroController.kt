package de.robolab.client.app.controller

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.TerminalInputViewModel
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.events.KeyEvent
import de.robolab.client.repl.*
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.RobolabJson
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

        override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion

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

            override fun fromToken(token: String): KeyBinding? {
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
    )

    @Serializable
    data class MacroStorage(
        val macroList: List<Macro>
    )

    private val macroList = mutableListOf<Macro>()

    fun onKeyDown(event: KeyEvent) {
        val binding = KeyBinding.fromKeyEvent(event)

        val macro = macroList.find { it.keyBinding == binding } ?: return

        GlobalScope.launch {
            for (line in macro.commands) {
                ReplExecutor.execute(line, DummyReplOutput)
            }
        }
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
            loadDefaults()
            save()
        }
    }

    private fun loadDefaults() {
        macroList.clear()

        macroList += Macro(
            KeyBinding(KeyCode.ENTER, ctrlKey = true),
            listOf("ui toggle terminal")
        )
    }

    init {
        load()

        ReplRootCommand.node("macro", "Interact with the macro system") {
            action("list", "List all available macros") { output ->
                for (macro in macroList) {
                    output.writeln(macro.keyBinding.toString())
                    for (c in macro.commands) {
                        output.write("  ")
                        output.writeHighlightCommand(c)
                        output.writeln()
                    }
                }
            }

            action(
                "get",
                "Show all commands for the given key binding",
                KeyBinding.param("binding"),
            ) { output, params ->
                val keyBinding = params[0] as KeyBinding

                val existing = macroList.find {
                    it.keyBinding == keyBinding
                }?.commands ?: emptyList()

                output.writeln(keyBinding.toString())
                for (c in existing) {
                    output.write("  ")
                    output.writeHighlightCommand(c)
                    output.writeln()
                }
            }

            action(
                "add",
                "Add a new command. If a macro with the given key binding already exists, the given command will be added to the existing macro",
                KeyBinding.param("binding"),
                StringParameter.param("command")
            ) { output, params ->
                val keyBinding = params[0] as KeyBinding
                val command = params[1] as StringParameter

                val existing = macroList.find {
                        it.keyBinding == keyBinding
                    }?.commands ?: emptyList()

                macroList.removeAll {
                    it.keyBinding == keyBinding
                }
                val macro = Macro(keyBinding, existing + command.value)
                macroList += macro
                save()

                output.writeln(macro.keyBinding.toString())
                for (c in macro.commands) {
                    output.write("  ")
                    output.writeHighlightCommand(c)
                    output.writeln()
                }
            }

            action(
                "remove",
                "Remove an existing command",
                KeyBinding.param("binding"),
                IntParameter.param("index", optional = true)
            ) { output, params ->
                val keyBinding = params[0] as KeyBinding
                val index = (params.getOrNull(1) as IntParameter?)?.value

                val existing = macroList.find {
                    it.keyBinding == keyBinding
                }?.commands ?: emptyList()


                macroList.removeAll {
                    it.keyBinding == keyBinding
                }

                if (index != null && index >= 0 && index < existing.size && existing.size > 1) {
                    val l = existing.toMutableList()
                    l.removeAt(index)

                    val macro = Macro(keyBinding, l)
                    macroList += macro

                    output.writeln(macro.keyBinding.toString())
                    for (c in macro.commands) {
                        output.write("  ")
                        output.writeHighlightCommand(c)
                        output.writeln()
                    }
                }

                save()
            }

            action("restore-defaults", "Delete all saved macros and restore the default bindings") { _ ->
                loadDefaults()
            }
        }
    }
}

fun IReplOutput.writeHighlightCommand(input: String) {
    writeIcon(MaterialIcon.CHEVRON_RIGHT)
    write(" ")

    val hint = ReplExecutor.hint(input)
    val list = buildList<TerminalInputViewModel.HintContent> {
        var lastSplit = 0

        for ((range, color) in hint.highlight) {
            add(TerminalInputViewModel.HintContent(
                hint.input.substring(lastSplit, range.first),
                null,
                lastSplit until range.first
            ))
            add(TerminalInputViewModel.HintContent(
                input.substring(range),
                color,
                range
            ))
            lastSplit = range.last + 1
        }

        add(TerminalInputViewModel.HintContent(
            input.substring(lastSplit, input.length),
            null,
            lastSplit until input.length
        ))
    }.filter { it.value.isNotEmpty() }

    for (item in list) {
        write(item.value, item.color?.toColor())
    }
}
