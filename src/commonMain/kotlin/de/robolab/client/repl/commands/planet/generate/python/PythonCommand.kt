package de.robolab.client.repl.commands.planet.generate.python

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand

object PythonCommand : ReplSingleBindableNodeCommand<FilePlanetDocument>(
    "python",
    "Generate python code based on the current planet",
    FilePlanetDocument::class,
) {

    init {
        addCommand(PythonAddPathsCommand)
    }
}
