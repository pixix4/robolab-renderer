package de.robolab.client.repl.commands.planet.edit.move

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplBoundCommandNodeTemplate
import de.robolab.client.repl.base.IReplBoundCommandTemplate

object MoveCommand : ReplBoundCommandNodeTemplate<FilePlanetDocument>(
    "move",
    "Move planet features around"
) {
    override val children: List<IReplBoundCommandTemplate<FilePlanetDocument>> = listOf(
        MoveVertexCommand,
        MovePointCommand
    )
}
