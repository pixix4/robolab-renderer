package de.robolab.client.repl.commands.planet

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand
import de.robolab.client.repl.commands.planet.edit.EditCommand
import de.robolab.client.repl.commands.planet.generate.GenerateCommand
import de.robolab.common.planet.PlanetPathVertex

object PlanetCommand : ReplSingleBindableNodeCommand<FilePlanetDocument>(
    "planet",
    "Operations related to the currently selected planet",
    FilePlanetDocument::class,
) {

    init {
        addCommand(EditCommand)
        addCommand(GenerateCommand)
    }

    override suspend fun requestAutoCompleteFor(
        binding: FilePlanetDocument,
        descriptor: ReplCommandParameterDescriptor<*>,
        token: String,
    ): List<ReplExecutor.AutoComplete>? {
        if (descriptor.type is PlanetPathVertex.Companion) {
            val pointEnd = binding.drawableProperty.value?.requestContext?.requestPointEnd() ?: return emptyList()

            return listOf(ReplExecutor.AutoComplete(
                PlanetPathVertex(pointEnd.first, pointEnd.second).toToken(),
                "Selected point end"
            ))
        }

        return null
    }
}
