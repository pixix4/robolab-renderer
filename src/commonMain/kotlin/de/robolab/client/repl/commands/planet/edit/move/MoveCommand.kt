package de.robolab.client.repl.commands.planet.edit.move

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand

object MoveCommand : ReplSingleBindableNodeCommand<FilePlanetDocument>(
    "move",
    "Move planet features around",
    FilePlanetDocument::class,
) {

    init {
        addCommand(MoveVertexCommand)
        addCommand(MovePointCommand)
    }
}
