package de.robolab.client.traverser

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.Planet

data class TraverserRenderState(
    val planet: Planet,
    val robotDrawable: RobotDrawable.Robot,
    val paths: List<Pair<PlanetPoint, PlanetDirection>>,
    val mothershipState: IMothershipState,
    val navigatorState: INavigatorState,
    val trail: ITraverserTrail
)
