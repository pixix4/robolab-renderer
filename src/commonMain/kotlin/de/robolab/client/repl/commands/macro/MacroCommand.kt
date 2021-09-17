package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.buildHintContent
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand

object MacroCommand : ReplSingleBindableNodeCommand<MacroController>(
    "macro",
    "Interact with the macro system",
    MacroController::class,
) {

    init {
        addCommand(MacroAddCommand)
        addCommand(MacroDebugCommand)
        addCommand(MacroExecuteCommand)
        addCommand(MacroGetCommand)
        addCommand(MacroListCommand)
        addCommand(MacroRemoveCommand)
        addCommand(MacroRestoreCommand)
    }
}

fun IReplOutput.writeHighlightCommand(input: String) {
    writeIcon(MaterialIcon.CHEVRON_RIGHT)
    write(" ")

    val hint = ReplExecutor.hint(input)
    val list = buildHintContent(hint.input, hint.highlight)

    for (item in list) {
        write(item.value, item.color)
    }
}
