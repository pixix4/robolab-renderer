package de.robolab.client.app.model.file.details

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.letter
import de.westermann.kobserve.property.observeConst

class PointDetailBox(point: PointAnimatableManager.AttributePoint, planetFile: PlanetFile): ViewModel{

    val coordinate = point.coordinate

    val position = "${coordinate.x}, ${coordinate.y}"
    private val isHidden = point.hidden

    val pathSelect = planetFile.planet.pathSelectList.filter {
        it.point == coordinate
    }.map {
        it.direction.name.toLowerCase().capitalize()
    }

    private val targetsSend = planetFile.planet.targetList.filter {
        it.exposure == coordinate
    }.map {
        "${it.target.x}, ${it.target.y}"
    }

    private val targetExposedAt = planetFile.planet.targetList.filter {
        it.target == coordinate
    }.map {
        "${it.exposure.x}, ${it.exposure.y}"
    }

    private val pathSend = planetFile.planet.pathList.filter {
        coordinate in it.exposure
    }.map {
        "${it.source.x},${it.source.y},${it.sourceDirection.letter()} -> " +
                "${it.target.x},${it.target.y},${it.targetDirection.letter()}"
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
