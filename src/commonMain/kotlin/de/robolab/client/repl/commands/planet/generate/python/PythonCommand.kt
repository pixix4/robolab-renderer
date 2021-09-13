package de.robolab.client.repl.commands.planet.generate.python

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplBoundCommandNodeTemplate
import de.robolab.client.repl.base.IReplBoundCommandTemplate

object PythonCommand : ReplBoundCommandNodeTemplate<FilePlanetDocument>(
    "python",
    "Generate python code based on the current planet"
) {
    override val children: List<IReplBoundCommandTemplate<FilePlanetDocument>> = listOf(
        PythonAddPathsCommand,
    )
}
