package de.robolab.client.app.model.file.details

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.utils.PathClassification
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.parser.PlanetFile
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Path
import de.robolab.common.planet.PlanetVersion
import de.robolab.common.planet.letter
import de.westermann.kobserve.property.property
import kotlin.math.roundToInt

class PathDetailBox(path: Path, planetFile: PlanetFile): ViewModel {

    val source = "${path.source.x}, ${path.source.y}, ${path.sourceDirection.letter()}"
    val target = "${path.target.x}, ${path.target.y}, ${path.targetDirection.letter()}"

    private val isHiddenProperty = property(getter = {
        path.hidden
    }, setter = {
        planetFile.togglePathHiddenState(path)
    })
    private val weightProperty = property(getter = {
        path.weight ?: 0
    }, setter = {
        planetFile.setPathWeight(path, it)
    })

    val length = getPathLengthString(planetFile.planet.version, path)

    private val classification = PathClassification.classify(planetFile.planet.version, path)

    private val pathExposedAt = path.exposure.map {
        "${it.x}, ${it.y}"
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
                input(weightProperty, -100000000..100000000)
            }
            labeledEntry("Hidden") {
                input(isHiddenProperty)
            }
            labeledEntry("Length") {
                input(length)
            }
        }
        labeledGroup("Classification") {
            labeledEntry("Classifier") {
                input(classification?.classifier?.desc ?: "")
            }
            labeledEntry("Difficulty") {
                input(classification?.difficulty?.name?.toLowerCase()?.capitalize() ?: "")
            }
            labeledEntry("Score") {
                input(classification?.score?.toString() ?: "")
            }
            labeledEntry("Curviness") {
                input(classification?.completeSegment?.curviness?.roundToInt()?.toString() ?: "")
            }
            labeledEntry("Details") {
                input(classification?.table ?: "")
            }
        }
        labeledGroup("Path exposed at") {
            for (i in pathExposedAt) {
                entry {
                    input(i)
                }
            }
        }
    }

    companion object {
        fun getPathLengthInGridUnits(planetVersion: PlanetVersion, path: Path): Double {
            return PathAnimatable.evalLength(planetVersion, path)
        }
        fun getPathLengthString(planetVersion: PlanetVersion, path: Path): String {
            val lengthGrid = getPathLengthInGridUnits(planetVersion, path)
            val lengthMeter = lengthGrid * PreferenceStorage.paperGridWidth
            return  "${lengthMeter.toFixed(2)} m\n${lengthGrid.toFixed(2)} units"
        }
    }
}
