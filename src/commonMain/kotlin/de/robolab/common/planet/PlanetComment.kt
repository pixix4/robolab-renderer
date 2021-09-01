package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable

@Serializable
data class PlanetComment(
    val lines: List<String>,
    val x: Double,
    val y: Double,
    val alignment: PlanetCommentAlignment,
) : IPlanetValue<PlanetComment> {

    constructor(
        lines: List<String>,
        coordinate: PlanetCoordinate,
        alignment: PlanetCommentAlignment
    ): this(lines, coordinate.x, coordinate.y, alignment)

    val coordinate: PlanetCoordinate
        get() = PlanetCoordinate(x, y)

    override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
        copy(x = p.x, y = p.y)
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
        Vector(x, y).rotate(direction.angle, origin.point).let { p ->
            copy(x = p.x, y = p.y)
        }
}
