package de.robolab.client.app.model.file.boxes

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.common.planet.PlanetFile
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.observeConst
import de.westermann.kobserve.property.property

class PointDetailBox(
    initPoint: PointAnimatableManager.AttributePoint,
    planetFile: PlanetFile,
) : ViewModel {


    val pointProperty = property(initPoint)
    var point by pointProperty

    val coordinate = pointProperty.mapBinding { it.coordinate }

    val position = coordinate.mapBinding { "${it.x}, ${it.y}" }
    private val isHidden = pointProperty.mapBinding { it.hidden }

    val pathSelect = coordinate.mapBinding { c ->
        planetFile.planet.pathSelects.filter {
            it.point == c
        }.joinToString("; ") { planetPathSelect ->
            planetPathSelect.direction.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private val targetsSend = coordinate.mapBinding { c ->
        planetFile.planet.targets.filter {
            c in it.exposure
        }.joinToString("; ") {
            "${it.x}, ${it.y}"
        }
    }

    private val targetExposedAt = coordinate.mapBinding { c ->
        planetFile.planet.targets.filter {
            it.point == c
        }.flatMap { t ->
            t.exposure.map {
                "${it.x}, ${it.y}"
            }
        }.joinToString("; ")
    }

    private val pathSend = coordinate.mapBinding { c ->
        planetFile.planet.paths.filter { path ->
            path.exposure.any { exposure ->
                exposure.planetPoint == c
            }
        }.joinToString("; ") {
            "${it.source.x},${it.source.y},${it.sourceDirection.letter} -> " +
                    "${it.target.x},${it.target.y},${it.targetDirection.letter}"
        }
    }

    val content = buildForm {
        labeledGroup("Point") {
            labeledEntry("Position") {
                input(position)
            }
            labeledEntry("Hidden") {
                input(isHidden)
            }
            labeledEntry("Path select") {
                input(pathSelect)
            }
            labeledEntry("Targets send") {
                input(targetsSend)
            }
            labeledEntry("Targets exposed at") {
                input(targetExposedAt)
            }
            labeledEntry("Path send") {
                input(pathSend)
            }
        }
    }
}
