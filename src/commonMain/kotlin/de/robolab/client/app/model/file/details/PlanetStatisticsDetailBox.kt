package de.robolab.client.app.model.file.details

import de.robolab.client.utils.PathClassifier
import de.robolab.client.utils.PlanetStatistic
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.parser.PlanetFile
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.StartPoint
import de.westermann.kobserve.property.mapBinding

class PlanetStatisticsDetailBox(planetFile: PlanetFile) {

    private val statisticsProperty = planetFile.planetProperty.mapBinding { PlanetStatistic(it) }

    private fun StartPoint.toFormattedString(): String {
        return "${point.x}, ${point.y}, ${orientation}"
    }

    val data = listOf(
        "General" to listOf(
            "Name" to planetFile.planetProperty.mapBinding { it.name },
            "Tags" to planetFile.planetProperty.mapBinding { it.tagMap.toList().joinToString("; ") { (key, value) -> "$key: $value" } },
            "Version" to planetFile.planetProperty.mapBinding { it.version.toString() },
            "Start point" to statisticsProperty.mapBinding { it.startPoint?.toFormattedString() ?: "" },
            "Path unveil count" to statisticsProperty.mapBinding { it.pathUnveilCount.toString() },
            "Paths select count" to statisticsProperty.mapBinding { it.pathSelectCount.toString() },
            "Target count" to statisticsProperty.mapBinding { it.targetCount.toString() },
            "Sender count" to statisticsProperty.mapBinding { it.senderCount.toString() },
        ),
        "Points" to listOf(
            "Point count" to statisticsProperty.mapBinding { it.pointCount.toString() },
            "Blue point count" to statisticsProperty.mapBinding { it.pointBlueCount.toString() },
            "Red point count" to statisticsProperty.mapBinding { it.pointRedCount.toString() },
            "Hidden point count" to statisticsProperty.mapBinding { it.pointHiddenCount.toString() },
            "Bottle count" to statisticsProperty.mapBinding { it.bottleCount.toString() },
        ),
        "Paths" to listOf(
            "Path count" to statisticsProperty.mapBinding { it.pathCount.toString() },
            "Free path count" to statisticsProperty.mapBinding { it.pathFreeCount.toString() },
            "Blocked path count" to statisticsProperty.mapBinding { it.pathBlockedCount.toString() },
            "Hidden path count" to statisticsProperty.mapBinding { it.pathHiddenCount.toString() },
            "Length" to planetFile.planetProperty.mapBinding { planet ->
                val lengthGrid = planet.pathList.sumByDouble {
                    PathDetailBox.getPathLengthInGridUnits(planet.version, it)
                }
                val lengthMeter = lengthGrid * PreferenceStorage.paperGridWidth
                "${lengthMeter.toFixed(2)}m (${lengthGrid.toFixed(2)} grid units)"
            },
        ),
        "Path classifiers" to listOf(
            *PathClassifier.values().map { classifier ->
                classifier.desc to statisticsProperty.mapBinding { (it.pathClassifier[classifier] ?: 0).toString() }
            }.toTypedArray(),
        )
    )
}