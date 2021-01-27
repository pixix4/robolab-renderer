package de.robolab.common.planet

import de.robolab.common.parser.serializeCoordinate
import de.robolab.common.parser.serializeDirection

sealed class SubscribableIdentifier<T> {

    abstract fun translate(delta: Coordinate): SubscribableIdentifier<T>
    abstract fun rotate(direction: Planet.RotateDirection, origin: Coordinate): SubscribableIdentifier<T>
    abstract fun lookup(planet: LookupPlanet, unify: Boolean = false): T
    abstract fun serialize(): String

    data class Path(
        val coordinate: Coordinate,
        val direction: Direction
    ) : SubscribableIdentifier<de.robolab.common.planet.Path>() {
        override fun translate(delta: Coordinate): Path =
            Path(coordinate.translate(delta), direction)

        override fun rotate(direction: Planet.RotateDirection, origin: Coordinate): Path =
            Path(coordinate.rotate(direction, origin), this.direction.rotate(direction))

        override fun lookup(planet: LookupPlanet, unify: Boolean): de.robolab.common.planet.Path {
            return if (!unify) {
                planet.getPath(coordinate, direction)
                    ?: error("Could not find path identified by $coordinate, $direction")
            } else {
                (planet.planet.pathList.firstOrNull { it.connectsWith(coordinate, direction) }
                    ?: error("Could not find path identified by $coordinate, $direction")).let {
                    if (it.source == coordinate && it.sourceDirection == direction) it
                    else it.reversed()
                }
            }
        }

        override fun serialize(): String = "${serializeCoordinate(coordinate)},${serializeDirection(direction)}"
    }

    data class Node(val coordinate: Coordinate) : SubscribableIdentifier<Coordinate>() {
        override fun translate(delta: Coordinate): Node =
            Node(coordinate.translate(delta))

        override fun rotate(direction: Planet.RotateDirection, origin: Coordinate): Node =
            Node(coordinate.rotate(direction, origin))

        override fun lookup(planet: LookupPlanet, unify: Boolean): Coordinate = coordinate

        override fun serialize(): String = serializeCoordinate(coordinate)
    }
}
