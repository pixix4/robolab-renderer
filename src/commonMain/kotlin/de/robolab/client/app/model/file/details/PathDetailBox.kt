package de.robolab.client.app.model.file.details

import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.utils.PathClassifier
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.parser.PlanetFile
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Path
import de.robolab.common.planet.PlanetVersion
import de.robolab.common.planet.letter
import de.westermann.kobserve.property.property

class PathDetailBox(path: Path, planetFile: PlanetFile) {

    val source = "${path.source.x}, ${path.source.y}, ${path.sourceDirection.letter()}"
    val target = "${path.target.x}, ${path.target.y}, ${path.targetDirection.letter()}"

    val isHiddenProperty = property(getter = {
        path.hidden
    }, setter = {
        planetFile.togglePathHiddenState(path)
    })
    val weightProperty = property(getter = {
        path.weight ?: 0
    }, setter = {
        planetFile.setPathWeight(path, it)
    })

    val length = getPathLengthString(planetFile.planet.version, path)

    val classifier = PathClassifier.classify(path).desc


    val pathExposedAt = path.exposure.map {
        "${it.x}, ${it.y}"
    }

    companion object {
        fun getPathLengthInGridUnits(planetVersion: PlanetVersion, path: Path): Double {
            return PathAnimatable.evalLength(planetVersion, path)
        }
        fun getPathLengthString(planetVersion: PlanetVersion, path: Path): String {
            val lengthGrid = getPathLengthInGridUnits(planetVersion, path)
            val lengthMeter = lengthGrid * PreferenceStorage.paperGridWidth
            return  "${lengthMeter.toFixed(2)}m (${lengthGrid.toFixed(2)} grid units)"
        }
    }
}
