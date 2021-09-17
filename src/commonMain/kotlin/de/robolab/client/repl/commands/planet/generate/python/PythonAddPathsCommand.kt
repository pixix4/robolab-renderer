package de.robolab.client.repl.commands.planet.generate.python

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.base.BooleanParameter
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.ReplFileType

object PythonAddPathsCommand : ReplBindableLeafCommand<FilePlanetDocument>(
    "add-paths",
    "Generate a python-function which adds all paths contained in this planet " +
            "to a python planet-object using its add_path() method",
    FilePlanetDocument::class,
) {

    private val indentTabsParam = BooleanParameter.optional("indent-tabs")

    override suspend fun execute(binding: FilePlanetDocument, context: IReplExecutionContext) {
        val indentTabs = context.getParameter(indentTabsParam)?.value ?: false

        val planet = binding.planetFile.planet
        val safeName = binding.planetFile.planet.name.replace(' ', '_')
        val indent = (if (indentTabs) "\t" else "    ")
        context.writeFile("add_paths_${safeName}.py", ReplFileType.TEXT) {
            (listOf("def add_paths_${safeName}(planet):") +
                    if (planet.paths.isEmpty()) listOf("${indent}pass")
                    else planet.paths.map {
                        "${indent}planet.add_path(" +
                                "((${it.sourceX}, ${it.sourceY}), Direction.${it.sourceDirection.value}), " +
                                "((${it.targetX}, ${it.targetY}), Direction.${it.targetDirection.value}), " +
                                "${it.weight})"
                    }).joinToString(separator = "\n")
        }
    }
}
