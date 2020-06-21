package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.IDetailBox
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.letter

class PointDetailBox(point: PointAnimatableManager.AttributePoint, planetFile: PlanetFile) :
    IDetailBox {

    val coordinate = point.coordinate

    val position = "${coordinate.x}, ${coordinate.y}"
    val isHidden = point.hidden

    val pathSelect = planetFile.planet.pathSelectList.filter {
        it.point == coordinate
    }.map {
        it.direction.name.toLowerCase().capitalize()
    }

    val targetsSend = planetFile.planet.targetList.filter {
        it.exposure == coordinate
    }.map {
        "${it.target.x}, ${it.target.y}"
    }

    val targetExposedAt = planetFile.planet.targetList.filter {
        it.target == coordinate
    }.map {
        "${it.exposure.x}, ${it.exposure.y}"
    }

    val pathSend = planetFile.planet.pathList.filter {
        coordinate in it.exposure
    }.map {
        "${it.source.x},${it.source.y},${it.sourceDirection.letter()} -> " +
                "${it.target.x},${it.target.y},${it.targetDirection.letter()}"
    }
}
