package de.robolab.client.traverser

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Planet

data class TraverserRenderState(
    val planet: Planet,
    val robotDrawable: RobotDrawable.Robot,
    val paths: List<Pair<Coordinate, Direction>>,
    val mothershipState: IMothershipState,
    val navigatorState: INavigatorState
)