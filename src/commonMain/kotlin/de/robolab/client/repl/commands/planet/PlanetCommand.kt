package de.robolab.client.repl.commands.planet

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplBoundCommandNodeTemplate
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.IReplBoundCommandTemplate
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.commands.planet.edit.EditCommand
import de.robolab.common.planet.PlanetPathVertex

object PlanetCommand : ReplBoundCommandNodeTemplate<FilePlanetDocument>(
    "planet",
    "Operations related to the currently selected planet"
) {
    override val children: List<IReplBoundCommandTemplate<FilePlanetDocument>> = listOf(
        EditCommand,
    )

    override suspend fun FilePlanetDocument.requestAutoCompleteFor(type: ReplCommandParameterDescriptor<*>): List<ReplExecutor.AutoComplete>? {
        if (type.type is PlanetPathVertex.Companion) {
            val pointEnd = drawableProperty.value?.requestContext?.requestPointEnd() ?: return emptyList()

            return listOf(ReplExecutor.AutoComplete(
                PlanetPathVertex(pointEnd.first, pointEnd.second).toToken(),
                "Selected point end"
            ))
        }

        return null
    }
}
