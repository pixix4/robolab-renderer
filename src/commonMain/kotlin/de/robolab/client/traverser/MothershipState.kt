package de.robolab.client.traverser

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.common.planet.*

interface IMothershipState {
    val sentTargets: Set<TargetPoint>
    val sentPaths: Set<Path>
    val sentPathSelects: Set<PathSelect>
    val isStart: Boolean
    val currentLocation: Coordinate
    val drivenPath: Path
    val currentTarget: TargetPoint?
    val newTargets: List<TargetPoint>
    val newPaths: List<Path>
    val selectedDirection: Direction?
    val forcedDirection: Direction?
    val beforePoint: Boolean
    val withAfterPoint: IMothershipState
}

fun IMothershipState.toDrawableRobot(groupNumber: Int? = null): RobotDrawable.Robot = RobotDrawable.Robot(
    currentLocation,
    forcedDirection ?: selectedDirection ?: drivenPath.targetDirection,
    beforePoint,
    groupNumber
)

data class MothershipState(
    override val sentTargets: Set<TargetPoint>,
    override val sentPaths: Set<Path>,
    override val sentPathSelects: Set<PathSelect>,
    val visitedLocations: Set<Coordinate>,
    override val isStart: Boolean,
    override val currentLocation: Coordinate,
    override val drivenPath: Path,
    override val currentTarget: TargetPoint?,
    override val newTargets: List<TargetPoint>,
    override val newPaths: List<Path>,
    override val selectedDirection: Direction?,
    override val forcedDirection: Direction?,
    override val beforePoint: Boolean
) : IMothershipState {

    override val withAfterPoint: MothershipState
        get() = copy(beforePoint = false)

    companion object Seed {
        fun getSeed(planet: Planet): MothershipState = MothershipState(
            sentTargets = emptySet(),
            sentPaths = emptySet(),
            sentPathSelects = emptySet(),
            visitedLocations = emptySet(),
            isStart = true,
            currentLocation = planet.startPoint?.point!!,
            drivenPath = Path(
                planet.startPoint.point, planet.startPoint.orientation.opposite(),
                planet.startPoint.point, planet.startPoint.orientation.opposite(),
                -1, emptySet(), emptyList(), hidden = false, showDirectionArrow = false
            ),
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