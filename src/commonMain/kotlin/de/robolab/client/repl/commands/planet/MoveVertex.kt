package de.robolab.client.repl.commands.planet

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplBoundParameterCommandTemplate
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.PlanetPathVertex

val MoveVertexCommand = ReplBoundParameterCommandTemplate<FilePlanetDocument>(
    "move_vertex",
    "Move all features (paths, pathSelects) from one point-vertex to another point-vertex",
    ReplCommandParameterDescriptor(PlanetPathVertex, "from"),
    ReplCommandParameterDescriptor(PlanetPathVertex, "to")
) { output, params ->
    val from: PlanetPathVertex = params[0] as PlanetPathVertex
    val to: PlanetPathVertex = params[1] as PlanetPathVertex

    val planet = planetFile.planet

    val paths: List<PlanetPath> = planet.paths.map {
        if (!it.connectsWith(from))
            return@map it
        val newSource = if (it.sourceVertex == from) to else it.sourceVertex
        val newTarget = if (it.targetVertex == from) to else it.targetVertex
        return@map it.copy(
            sourceX = newSource.point.x,
            sourceY = newSource.point.y,
            sourceDirection = newSource.direction,
            targetX = newTarget.point.x,
            targetY = newTarget.point.y,
            targetDirection = newTarget.direction
        )
    }

    planetFile.planet = planet.copy(paths = paths)
    //TODO: Move pathSelects, start-paths and optionally splines
}
