package de.roboplot.plotter.traverser

import de.roboplot.plotter.model.*
import de.roboplot.plotter.model.Target

interface IMothershipState {
    val isStart: Boolean
    val currentLocation: Point
    val drivenPath: Path
    val currentTarget: Target?
    val newTargets: List<Target>
    val newPaths: List<Path>
    val selectedDirection: Direction?
    val forcedDirection: Direction?
    val beforePoint: Boolean
}

data class MothershipState(
        val sentTargets: Set<Target>,
        val sentPaths: Set<Path>,
        val sentPathSelects: Set<PathSelect>,
        val visitedLocations: Set<Point>,
        override val isStart: Boolean,
        override val currentLocation: Point,
        override val drivenPath: Path,
        override val currentTarget: Target?,
        override val newTargets: List<Target>,
        override val newPaths: List<Path>,
        override val selectedDirection: Direction?,
        override val forcedDirection: Direction?,
        override val beforePoint: Boolean
) : IMothershipState {

    companion object Seed {
        fun getSeed(planet: Planet): MothershipState = MothershipState(
                sentTargets = emptySet(),
                sentPaths = emptySet(),
                sentPathSelects = emptySet(),
                visitedLocations = emptySet(),
                isStart = true,
                currentLocation = planet.start!!,
                drivenPath = Path(Path.From.SERVER,
                        planet.start, planet.startOrientation.opposite(),
                        planet.start, planet.startOrientation.opposite(),
                        -1, true),
                currentTarget = null,
                newTargets = emptyList(),
                newPaths = emptyList(),
                selectedDirection = null,
                forcedDirection = null,
                beforePoint = true
        )

        fun getSeed(planet: LookupPlanet): MothershipState = getSeed(planet.planet)
    }

}