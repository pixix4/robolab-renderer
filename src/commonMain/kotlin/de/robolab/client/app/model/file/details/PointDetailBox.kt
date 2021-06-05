package de.robolab.client.app.model.file.details

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.common.planet.PlanetFile
import de.westermann.kobserve.property.observeConst

class PointDetailBox(point: PointAnimatableManager.AttributePoint, planetFile: PlanetFile): ViewModel{

    val coordinate = point.coordinate

    val position = "${coordinate.x}, ${coordinate.y}"
    private val isHidden = point.hidden

    val pathSelect = planetFile.planet.pathSelects.filter {
        it.point == coordinate
    }.map {
        it.direction.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private val targetsSend = planetFile.planet.targets.filter {
        coordinate in it.exposure
    }.map {
        "${it.x}, ${it.y}"
    }

    private val targetExposedAt = planetFile.planet.targets.filter {
        it.point == coordinate
    }.flatMap { t ->
        t.exposure.map {
            "${it.x}, ${it.y}"
        }
    }

    private val pathSend = planetFile.planet.paths.filter {
        coordinate in it.exposure
    }.map {
        "${it.source.x},${it.source.y},${it.sourceDirection.letter} -> " +
                "${it.target.x},${it.target.y},${it.targetDirection.letter}"
    }

    val content = buildForm {
        labeledGroup("Point") {
            labeledEntry("Position") {
                input(position)
            }
            labeledEntry("Hidden") {
                input(isHidden.observeConst())
            }
        }
        labeledGroup("Targets send") {
            for (i in targetsSend) {
                entry {
                    input(i)
                }
            }
        }
        labeledGroup("Target exposed at") {
            for (i in targetExposedAt) {
                entry {
                    input(i)
                }
            }
        }
        labeledGroup("Path send") {
            for (i in pathSend) {
                entry {
                    input(i)
                }
            }
        }
    }
}
