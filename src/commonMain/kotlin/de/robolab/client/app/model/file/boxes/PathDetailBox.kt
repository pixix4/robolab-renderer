package de.robolab.client.app.model.file.boxes

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.utils.PathClassification
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.planet.PlanetFile
import de.robolab.common.planet.PlanetPath
import de.robolab.common.utils.toFixed
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.roundToInt

class PathDetailBox(
    initPath: PlanetPath,
    planetFile: PlanetFile,
) : ViewModel {

    val pathProperty = property(initPath)
    var path by pathProperty

    val source = pathProperty.mapBinding {
        "${it.source.x}, ${it.source.y}, ${it.sourceDirection.letter}"
    }
    val target = pathProperty.mapBinding {
        "${it.target.x}, ${it.target.y}, ${it.targetDirection.letter}"
    }

    private val isHiddenProperty = property(getter = {
        path.hidden
    }, setter = {
        planetFile.togglePathHiddenState(path)
    }, pathProperty)
    private val weightProperty = property(getter = {
        path.weight
    }, setter = {
        planetFile.setPathWeight(path, it)
    }, pathProperty)

    val length = pathProperty.mapBinding {
        getPathLengthString(planetFile.planet.version, it)
    }

    private val classification = pathProperty.mapBinding {
        PathClassification.classify(planetFile.planet.version, it)
    }

    private val pathExposedAt = pathProperty.mapBinding { p ->
        p.exposure.joinToString("; ") {
            "(${it.x}, ${it.y})"
        }
    }

    val content = buildForm {
        labeledGroup("Path") {
            labeledEntry("Source") {
                input(source)
            }
            labeledEntry("Target") {
                input(target)
            }
            labeledEntry("Weight") {
                input(weightProperty, -100000000L..100000000L)
            }
            labeledEntry("Hidden") {
                input(isHiddenProperty)
            }
            labeledEntry("Length") {
                input(length)
            }
            labeledEntry("Exposure") {
                input(pathExposedAt)
            }
        }
        labeledGroup("Classification") {
            labeledEntry("Classifier") {
                input(classification.mapBinding { it?.classifier?.desc ?: "" })
            }
            labeledEntry("Difficulty") {
                input(classification.mapBinding {
                    it?.difficulty?.name?.lowercase()
                        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        ?: ""
                })
            }
            labeledEntry("Score") {
                input(classification.mapBinding { it?.score?.toString() ?: "" })
            }
            labeledEntry("Curviness") {
                input(classification.mapBinding { it?.completeSegment?.curviness?.roundToInt()?.toString() ?: "" })
            }
        }
    }

    companion object {
        fun getPathLengthInGridUnits(planetVersion: Long, path: PlanetPath): Double {
            return PathAnimatable.evalLength(planetVersion, path)
        }

        fun getPathLengthString(planetVersion: Long, path: PlanetPath): String {
            val lengthGrid = getPathLengthInGridUnits(planetVersion, path)
            val lengthMeter = lengthGrid * PreferenceStorage.paperGridWidth
            return "${lengthMeter.toFixed(2)} m\n${lengthGrid.toFixed(2)} units"
        }
    }
}
