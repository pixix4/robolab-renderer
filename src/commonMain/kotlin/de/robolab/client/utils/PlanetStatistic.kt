package de.robolab.client.utils

import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

class PlanetStatistic(
    val planet: Planet
) {
    val pathCount = planet.pathList.size
    val pathBlockedCount = planet.pathList.count { it.blocked || it.weight != null && it.weight < 0.0 }
    val pathFreeCount = pathCount - pathBlockedCount
    val pathHiddenCount = planet.pathList.count { it.hidden }

    val bottleCount = planet.pathList.filter {
        it.blocked || it.weight != null && it.weight < 0.0
    }.map {
        PathAnimatable.getControlPointsFromPath(planet.version, it).lastOrNull() ?: it.target.toPoint()
    }.distinctBy {
        it.roundedWithMultiplier(10.0)
    }.count()

    private val points = planet.getPointList()

    val pointCount = points.size
    val pointRedCount = points.count {
        !PointAnimatableManager.isPointHidden(
            planet,
            it
        ) && it.getColor(planet.bluePoint) == Coordinate.Color.RED
    }
    val pointBlueCount = points.count {
        !PointAnimatableManager.isPointHidden(
            planet,
            it
        ) && it.getColor(planet.bluePoint) == Coordinate.Color.BLUE
    }
    val pointHiddenCount = points.count { PointAnimatableManager.isPointHidden(planet, it) }

    val startPoint = planet.startPoint
    val pathUnveilCount = planet.pathList.count { it.exposure.isNotEmpty() }
    val pathSelectCount = planet.pathSelectList.size
    val targetCount = planet.targetList.groupBy { it.target }.size
    val senderCount = planet.senderGrouping.keys.flatten().distinct().size

    val classification = planet.pathList.map { PathClassification.classify(planet.version, it) }

    val pathDifficulty = classification
        .groupBy {
            it?.difficulty
        }.mapValues { (_, list) ->
            list.size
        }

    val pathClassifier = classification
        .groupBy {
            it?.classifier
        }.mapValues { (_, list) ->
            list.size
        }
}