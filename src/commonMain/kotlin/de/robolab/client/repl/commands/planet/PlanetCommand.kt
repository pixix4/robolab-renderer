package de.robolab.client.repl.commands.planet

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplBoundCommandNodeTemplate
import de.robolab.client.repl.base.IReplBoundCommandTemplate
import de.robolab.client.repl.commands.planet.edit.EditCommand

object PlanetCommand : ReplBoundCommandNodeTemplate<FilePlanetDocument>(
    "planet",
    "Operations related to the currently selected planet"
) {
    override val children: List<IReplBoundCommandTemplate<FilePlanetDocument>> = listOf(
        EditCommand,
    )
}
