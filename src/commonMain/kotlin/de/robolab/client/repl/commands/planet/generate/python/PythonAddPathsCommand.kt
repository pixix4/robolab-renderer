package de.robolab.client.repl.commands.planet.generate.python

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.BooleanParameter
import de.robolab.client.repl.ReplBoundParameterCommandTemplate
import de.robolab.client.repl.base.FileType
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplOutput

object PythonAddPathsCommand : ReplBoundParameterCommandTemplate<FilePlanetDocument>(
    "add-paths",
    "Generate a python-function which adds all paths contained in this planet " +
            "to a python planet-object using its add_path() method"
) {
    override suspend fun FilePlanetDocument.execute(out: IReplOutput, params: List<IReplCommandParameter>) {
        val planet = planetFile.planet
        val safeName = planetFile.planet.name.replace(' ', '_')
        val indentTabs = (params.getOrNull(1) as BooleanParameter?)?.value ?: false
        val indent = (if (indentTabs) "\t" else "    ")
        out.writeFile("add_paths_${safeName}.py", FileType.TEXT) {
            (listOf("def add_paths_${safeName}(planet):") +
                    if(planet.paths.isEmpty()) listOf("${indent}pass")
                    else planet.paths.map {
                "${indent}planet.add_path(" +
                        "((${it.sourceX}, ${it.sourceY}), Direction.${it.sourceDirection.value}), " +
                        "((${it.targetX}, ${it.targetY}), Direction.${it.targetDirection.value}), " +
                        "${it.weight})"
            }).joinToString(separator = "\n")
        }
    }
}
