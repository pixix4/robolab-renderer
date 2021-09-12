package de.robolab.client.repl.commands.planet.edit.move

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.renderer.drawable.live.toAngle
import de.robolab.client.repl.BooleanParameter
import de.robolab.client.repl.ReplBoundParameterCommandTemplate
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.common.planet.*
import de.robolab.common.utils.getInterpolated

object MoveVertexCommand : ReplBoundParameterCommandTemplate<FilePlanetDocument>(
    "vertex",
    "Move all features (paths, pathSelects) from one point-vertex to another point-vertex",
    ReplCommandParameterDescriptor(PlanetPathVertex, "from"),
    ReplCommandParameterDescriptor(PlanetPathVertex, "to"),
    ReplCommandParameterDescriptor(BooleanParameter, "transformSpline", true)
) {
    override suspend fun FilePlanetDocument.execute(out: IReplOutput, params: List<IReplCommandParameter>) {

        val from: PlanetPathVertex = params[0] as PlanetPathVertex
        val to: PlanetPathVertex = params[1] as PlanetPathVertex
        val transformSpline: Boolean = (params.getOrNull(2) as BooleanParameter?)?.value ?: false

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
                targetDirection = newTarget.direction,
                spline = if (transformSpline) it.spline?.moveByVertices(
                    it.sourceVertex to it.targetVertex,
                    newSource to newTarget,
                ) else it.spline
            )
        }

        val startPoint: PlanetStartPoint = if (planet.startPoint.vertex == from) planet.startPoint.copy(
            x = to.point.x,
            y = to.point.y,
            orientation = to.direction.opposite(),
            spline = if (transformSpline) planet.startPoint.spline?.moveByVertices(
                planet.startPoint.vertex to planet.startPoint.vertex,
                to to to
            ) else planet.startPoint.spline
        ) else planet.startPoint

        val pathSelects: List<PlanetPathSelect> = planet.pathSelects.map {
            if (it.vertex == from) PlanetPathSelect(to) else it
        }

        planetFile.planet = planet.copy(paths = paths, startPoint = startPoint, pathSelects = pathSelects)
    }

    override suspend fun FilePlanetDocument.requestAutoCompleteFor(type: ReplCommandParameterDescriptor<*>): List<ReplExecutor.AutoComplete> {
        if (type.type is PlanetPathVertex.Companion) {
            val pointEnd = drawableProperty.value?.requestContext?.requestPointEnd() ?: return emptyList()

            return listOf(ReplExecutor.AutoComplete(
                PlanetPathVertex(pointEnd.first, pointEnd.second).toToken(),
                "Selected point end"
            ))
        }

        return emptyList()
    }

    fun PlanetSpline.moveByVertices(
        old: Pair<PlanetPathVertex, PlanetPathVertex>,
        new: Pair<PlanetPathVertex, PlanetPathVertex>,
        scaleNormal: Boolean = true,
        weightMode: SplineMoveWeightMode = SplineMoveWeightMode.PROJECTION,
    ) = moveByVertices(old, new, scaleNormal, weightMode::getWeight)

    inline fun PlanetSpline.moveByVertices(
        old: Pair<PlanetPathVertex, PlanetPathVertex>,
        new: Pair<PlanetPathVertex, PlanetPathVertex>,
        scaleNormal: Boolean = true,
        weightFunction: (PlanetSpline, Double, Pair<PlanetPathVertex, PlanetPathVertex>) -> Double,
    ): PlanetSpline = copy(
        controlPoints = this.controlPoints.mapIndexed { index, cp ->
            val offsetOldSource = (cp.point - old.first.point.point).rotate(-old.first.direction.toAngle())
            val offsetOldTarget = (cp.point - old.second.point.point).rotate(-old.second.direction.toAngle())

            val scaleFactor =
                if (scaleNormal && old.first.point.point != old.second.point.point)
                    new.first.point.point.distanceTo(
                        new.second.point.point
                    ) / old.first.point.point.distanceTo(
                        old.second.point.point
                    ) else 1

            val offsetNewSource = (offsetOldSource * scaleFactor).rotate(new.first.direction.toAngle())
            val offsetNewTarget = (offsetOldTarget * scaleFactor).rotate(new.second.direction.toAngle())
            val sourceBasedNewPosition = new.first.point.point + offsetNewSource
            val targetBasedNewPosition = new.second.point.point + offsetNewTarget
            val weight = weightFunction(this, index.toDouble() / (this.controlPoints.lastIndex), old)
            sourceBasedNewPosition.interpolate(targetBasedNewPosition, weight).planetCoordinate
        }
    )

    enum class SplineMoveWeightMode {
        PROGRESS {
            override fun getWeight(
                spline: PlanetSpline,
                progress: Double,
                path: Pair<PlanetPathVertex, PlanetPathVertex>,
            ): Double = progress
        },
        PROJECTION {
            override fun getWeight(
                spline: PlanetSpline,
                progress: Double,
                path: Pair<PlanetPathVertex, PlanetPathVertex>,
            ): Double {
                if (progress < 0 || progress > 1) throw IllegalArgumentException("Progress-Parameter out of range (0..1): $progress")
                //TODO: Use more accurate position calculation
                val position =
                    spline.controlPoints.map(PlanetCoordinate::point)
                        .getInterpolated(progress * (spline.controlPoints.lastIndex))
                val base = path.second.point.point - path.first.point.point
                return (position - path.first.point.point).projectOnto(base).first
            }
        },
        DISTANCE {
            override fun getWeight(
                spline: PlanetSpline,
                progress: Double,
                path: Pair<PlanetPathVertex, PlanetPathVertex>,
            ): Double {
                if (progress < 0 || progress > 1) throw IllegalArgumentException("Progress-Parameter out of range (0..1): $progress")
                //TODO: Use more accurate position calculation
                val position =
                    spline.controlPoints.map(PlanetCoordinate::point)
                        .getInterpolated(progress * (spline.controlPoints.lastIndex))
                val dist1 = path.first.point.point.distanceTo(position)
                val dist2 = path.second.point.point.distanceTo(position)
                if (dist1 == 0.0) return 0.0
                return dist1 / (dist1 + dist2)
            }
        };

        abstract fun getWeight(
            spline: PlanetSpline,
            progress: Double,
            path: Pair<PlanetPathVertex, PlanetPathVertex>,
        ): Double
    }
}
