package de.robolab.client.app.model.file.details

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.utils.PathClassification
import de.robolab.client.utils.PlanetStatistic
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.parser.PlanetFile
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.StartPoint
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.observeConst

class PlanetStatisticsDetailBox(planetFile: PlanetFile) : ViewModel {

    private val statisticsProperty = planetFile.planetProperty.mapBinding { PlanetStatistic(it) }

    private fun StartPoint.toFormattedString(): String {
        return "${point.x}, ${point.y}, $orientation"
    }

    val data: List<Pair<String, ObservableValue<List<Pair<String, ObservableValue<String>>>>>> = listOf(
        "General" to listOf(
            "Name" to planetFile.planetProperty.mapBinding { it.name },
            "Version" to planetFile.planetProperty.mapBinding { it.version.toString() },
            "Start point" to statisticsProperty.mapBinding { it.startPoint?.toFormattedString() ?: "" },
            "Path unveil count" to statisticsProperty.mapBinding { it.pathUnveilCount.toString() },
            "Paths select count" to statisticsProperty.mapBinding { it.pathSelectCount.toString() },
            "Target count" to statisticsProperty.mapBinding { it.targetCount.toString() },
            "Sender count" to statisticsProperty.mapBinding { it.senderCount.toString() },
        ).observeConst(),
        "Tags" to planetFile.planetProperty.mapBinding { planet ->
            planet.tagMap.mapValues { (_, value) ->
                value.joinToString("\", \"", "\"", "\"").observeConst()
            }.toList().sortedBy { it.first }
        },
        "Points" to listOf(
            "Point count" to statisticsProperty.mapBinding { it.pointCount.toString() },
            "Blue point count" to statisticsProperty.mapBinding { it.pointBlueCount.toString() },
            "Red point count" to statisticsProperty.mapBinding { it.pointRedCount.toString() },
            "Hidden point count" to statisticsProperty.mapBinding { it.pointHiddenCount.toString() },
            "Bottle count" to statisticsProperty.mapBinding { it.bottleCount.toString() },
        ).observeConst(),
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
                "${lengthMeter.toFixed(2)} m\n${lengthGrid.toFixed(2)} units"
            },
        ).observeConst(),
        "Path difficulty" to PathClassification.Difficulty.values().map { difficulty ->
            difficulty.name.toLowerCase().capitalize() to statisticsProperty.mapBinding {
                val count = it.pathDifficulty[difficulty] ?: 0
                if (count <= 0) "" else count.toString()
            }
        }.toList().observeConst(),
        "Path classifiers" to PathClassification.Classifier.values().map { classifier ->
            classifier.desc to statisticsProperty.mapBinding {
                val count = it.pathClassifier[classifier] ?: 0
                if (count <= 0) "" else count.toString()
            }
        }.toList().observeConst(),
        "Test suite" to listOf(
            "Goals" to planetFile.planetProperty.mapBinding {
                if (it.testSuite.goals.size <= 3) it.testSuite.goals.joinToString()
                else it.testSuite.goals.size.toString()
            },
            "Tasks" to planetFile.planetProperty.mapBinding { it.testSuite.taskList.size.toString() },
            "Triggers" to planetFile.planetProperty.mapBinding { it.testSuite.triggerList.size.toString() },
            "Flag setters" to planetFile.planetProperty.mapBinding { it.testSuite.flagSetterList.size.toString() },
        ).observeConst()
    )

    val content = buildForm {
        for ((label, block) in data) {
            if (block.value.isNotEmpty()) {
                labeledGroup(label) {
                    for ((key, value) in block.value) {
                        labeledEntry(key) {
                            input(value)
                        }
                    }
                }
            }
        }
    }
}
