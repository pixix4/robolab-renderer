package de.robolab.client.traverser

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.common.planet.*
import de.robolab.common.planet.utils.LookupPlanet

interface IMothershipState {
    val sentTargets: Set<PlanetTarget>
    val sentPaths: Set<PlanetPath>
    val sentPathSelects: Set<PlanetPathSelect>
    val isStart: Boolean
    val currentLocation: PlanetPoint
    val drivenPath: PlanetPath
    val currentTarget: PlanetTarget?
    val newTargets: List<PlanetTarget>
    val newPaths: List<PlanetPath>
    val selectedDirection: PlanetDirection?
    val forcedDirection: PlanetDirection?
    val beforePoint: Boolean
    val withAfterPoint: IMothershipState
}

fun IMothershipState.toDrawableRobot(isBackward: Boolean = false, groupNumber: Int? = null): RobotDrawable.Robot = RobotDrawable.Robot(
    currentLocation,
    forcedDirection ?: selectedDirection ?: drivenPath.targetDirection,
    beforePoint,
    groupNumber,
    isBackward
)

data class MothershipState(
    override val sentTargets: Set<PlanetTarget>,
    override val sentPaths: Set<PlanetPath>,
    override val sentPathSelects: Set<PlanetPathSelect>,
    val visitedLocations: Set<PlanetPoint>,
    override val isStart: Boolean,
    override val currentLocation: PlanetPoint,
    override val drivenPath: PlanetPath,
    override val currentTarget: PlanetTarget?,
    override val newTargets: List<PlanetTarget>,
    override val newPaths: List<PlanetPath>,
    override val selectedDirection: PlanetDirection?,
    override val forcedDirection: PlanetDirection?,
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
            currentLocation = planet.startPoint.point,
            drivenPath = PlanetPath(
                sourceX = planet.startPoint.x,
                sourceY = planet.startPoint.y,
                sourceDirection = planet.startPoint.orientation.opposite(),
                targetX = planet.startPoint.x,
                targetY = planet.startPoint.y,
                targetDirection = planet.startPoint.orientation.opposite(),
                weight = 0L,
                exposure = emptySet(),
                hidden = false,
                spline = null,
                arrow = false
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
