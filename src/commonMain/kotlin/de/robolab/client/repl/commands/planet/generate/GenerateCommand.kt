package de.robolab.client.repl.commands.planet.generate

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand
import de.robolab.client.repl.commands.planet.generate.python.PythonCommand

object GenerateCommand : ReplSingleBindableNodeCommand<FilePlanetDocument>(
    "generate",
    "Generate files from the current planet",
    FilePlanetDocument::class,
) {

    init {
        addCommand(PythonCommand)
    }
}
