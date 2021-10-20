package de.robolab.client.repl.commands.planet.edit

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand
import de.robolab.client.repl.commands.planet.edit.move.MoveCommand

object EditCommand : ReplSingleBindableNodeCommand<FilePlanetDocument>(
    "edit",
    "Manipulate the currently selected planet",
    FilePlanetDocument::class,
) {

    init {
        addCommand(MoveCommand)
    }
}
