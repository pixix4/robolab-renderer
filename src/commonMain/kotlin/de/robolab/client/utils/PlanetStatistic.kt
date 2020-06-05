package de.robolab.client.utils

import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.utils.Utils
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class PlanetStatistic(
    val planet: Planet
) {
    val pathCount = planet.pathList.size
    val pathBlockedCount = planet.pathList.count { it.blocked || it.weight != null && it.weight < 0.0 }
    val pathFreeCount = pathCount - pathBlockedCount
    val pathHiddenCount = planet.pathList.count { it.hidden }

    private val points = PointAnimatableManager.getPointList(planet)

    val pointCount = points.size
    val pointRedCount = points.count { it.getColor(planet.bluePoint) == Coordinate.Color.RED }
    val pointBlueCount = points.count { it.getColor(planet.bluePoint) == Coordinate.Color.BLUE }
    val pointHiddenCount = points.count { PointAnimatableManager.isPointHidden(planet, it) }

    val startPoint = planet.startPoint
    val pathUnveilCount = planet.pathList.count { it.exposure.isNotEmpty() }
    val pathSelectCount = planet.pathSelectList.size
    val targetCount = planet.targetList.groupBy { it.target }.size
    val senderCount = Utils.getSenderGrouping(planet).keys.flatten().distinct().size

    val pathClassifier = planet.pathList
        .groupBy {
            PathClassifier.classify(it)
        }.mapValues { (_, list) ->
            list.size
        }
}