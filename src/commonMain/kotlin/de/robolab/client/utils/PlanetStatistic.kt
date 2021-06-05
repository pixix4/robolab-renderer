package de.robolab.client.utils

import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint

class PlanetStatistic(
    val planet: Planet,
) {
    val pathCount = planet.paths.size
    val pathBlockedCount = planet.paths.count { it.blocked || it.weight < 0.0 }
    val pathFreeCount = pathCount - pathBlockedCount
    val pathHiddenCount = planet.paths.count { it.hidden }

    val bottleCount = planet.paths.filter {
        it.blocked || it.weight < 0.0
    }.map {
        PathAnimatable.getControlPointsFromPath(planet.version, it).lastOrNull() ?: it.target.point
    }.distinctBy {
        it.roundedWithMultiplier(10.0)
    }.count()

    private val points = planet.getPointList()

    val pointCount = points.size
    val pointRedCount = points.count {
        !PointAnimatableManager.isPointHidden(
            planet,
            it
        ) && it.getColor(planet.bluePoint) == PlanetPoint.Color.Red
    }
    val pointBlueCount = points.count {
        !PointAnimatableManager.isPointHidden(
            planet,
            it
        ) && it.getColor(planet.bluePoint) == PlanetPoint.Color.Blue
    }
    val pointHiddenCount = points.count { PointAnimatableManager.isPointHidden(planet, it) }

    val startPoint = planet.startPoint
    val pathUnveilCount = planet.paths.count { it.exposure.isNotEmpty() }
    val pathSelectCount = planet.pathSelects.size
    val targetCount = planet.targets.groupBy { it.point }.size
    val senderCount = planet.senderGroupingsMap.keys.flatten().distinct().size

    val classification = planet.paths.map { PathClassification.classify(planet.version, it) }

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
