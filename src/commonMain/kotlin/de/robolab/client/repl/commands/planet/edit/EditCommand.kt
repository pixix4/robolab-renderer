package de.robolab.client.repl.commands.planet.edit

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplBoundCommandNodeTemplate
import de.robolab.client.repl.base.IReplBoundCommandTemplate
import de.robolab.client.repl.commands.planet.edit.move.MoveCommand

object EditCommand : ReplBoundCommandNodeTemplate<FilePlanetDocument>(
    "edit",
    "Manipulate the currently selected planet"
) {
    override val children: List<IReplBoundCommandTemplate<FilePlanetDocument>> = listOf(
        MoveCommand,
    )
}
