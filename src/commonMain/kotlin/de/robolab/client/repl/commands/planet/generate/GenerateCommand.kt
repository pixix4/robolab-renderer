package de.robolab.client.repl.commands.planet.generate

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplBoundCommandNodeTemplate
import de.robolab.client.repl.base.IReplBoundCommandTemplate
import de.robolab.client.repl.commands.planet.generate.python.PythonCommand

object GenerateCommand : ReplBoundCommandNodeTemplate<FilePlanetDocument>(
    "generate",
    "Generate files from the current planet"
) {
    override val children: List<IReplBoundCommandTemplate<FilePlanetDocument>> = listOf(
        PythonCommand
    )
}
