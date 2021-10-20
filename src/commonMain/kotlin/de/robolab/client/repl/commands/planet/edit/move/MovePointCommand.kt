package de.robolab.client.repl.commands.planet.edit.move

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.base.BooleanParameter
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.commands.planet.edit.move.MoveVertexCommand.moveByVertices
import de.robolab.common.planet.*

object MovePointCommand : ReplBindableLeafCommand<FilePlanetDocument>(
    "point",
    "Move all features (paths, pathSelects) from one point to another point",
    FilePlanetDocument::class,
) {

    private val fromParam = PlanetPoint.param("from")
    private val toParam = PlanetPoint.param("to")
    private val transformSplineParam = BooleanParameter.optional("transformSpline")

    override suspend fun execute(binding: FilePlanetDocument, context: IReplExecutionContext) {
        val from = context.getParameter(fromParam)
        val to = context.getParameter(toParam)
        val transformSpline = context.getParameter(transformSplineParam)?.value ?: false

        val planet = binding.planetFile.planet

        val paths: List<PlanetPath> = planet.paths.map {
            if (!it.connectsWith(from) && !it.exposure.any { ex -> ex.planetPoint == from })
                return@map it
            val newSource = if (it.source == from) to else it.source
            val newTarget = if (it.target == from) to else it.target
            return@map it.copy(
                sourceX = newSource.x,
                sourceY = newSource.y,
                targetX = newTarget.x,
                targetY = newTarget.y,
                spline = if (transformSpline && it.connectsWith(from)) it.spline?.moveByVertices(
                    it.sourceVertex to it.targetVertex,
                    PlanetPathVertex(newSource, it.sourceDirection) to PlanetPathVertex(newTarget, it.targetDirection),
                ) else it.spline,
                exposure = it.exposure.map { ex -> if (ex.planetPoint == from) ex.copy(x = to.x, y = to.y) else ex }
                    .toSet()
            )
        }

        val oldStartPoint = planet.startPoint
        val startPoint: PlanetStartPoint = if (oldStartPoint.point == from) oldStartPoint.copy(
            x = to.x,
            y = to.y,
            spline = if (transformSpline) oldStartPoint.spline?.moveByVertices(
                oldStartPoint.vertex to oldStartPoint.vertex,
                PlanetPathVertex(to, oldStartPoint.orientation.opposite())
                        to PlanetPathVertex(
                    to, oldStartPoint.orientation.opposite()
                )
            ) else planet.startPoint.spline
        ) else planet.startPoint

        val pathSelects: List<PlanetPathSelect> = planet.pathSelects.map {
            if (it.point == from) PlanetPathSelect(to, it.direction) else it
        }

        val targets: List<PlanetTarget> = planet.targets.map {
            val newValue = if (it.point == from) to else it.point
            it.copy(
                x = newValue.x,
                y = newValue.y,
                exposure = if (from in it.exposure) ((it.exposure - from) + to) else it.exposure
            )
        }

        binding.planetFile.planet = planet.copy(
            paths = paths,
            startPoint = startPoint,
            pathSelects = pathSelects,
            targets = targets,
        )
    }
}
