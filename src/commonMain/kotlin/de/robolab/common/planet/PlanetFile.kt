package de.robolab.common.planet

import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.utils.History
import de.robolab.common.utils.Vector
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.math.exp

class PlanetFile(planet: Planet) : IEditCallback {

    constructor(content: String) : this(Companion.parse(content))

    val planetProperty = History(planet)
    var planet by planetProperty

    private fun setPlanet(planet: Planet, groupHistory: Boolean) {
        if (groupHistory) {
            planetProperty.replace(planet)
        } else {
            planetProperty.set(planet)
        }
    }

    fun parse(content: String, groupHistory: Boolean = false) {
        val p = Companion.parse(content)

        setPlanet(p, groupHistory)
    }

    fun stringify(): String {
        return stringify(planet)
    }

    override fun createPath(
        source: PlanetPoint,
        sourceDirection: PlanetDirection,
        target: PlanetPoint,
        targetDirection: PlanetDirection,
        spline: PlanetSpline?,
        groupHistory: Boolean,
    ) {
        val p = planet.copy(
            paths = planet.paths - PlanetPath(
                source = source,
                sourceDirection = sourceDirection,
                target = target,
                targetDirection = targetDirection,
                weight = 1L,
                exposure = emptySet(),
                hidden = false,
                spline = spline,
                arrow = false
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun updatePathSpline(path: PlanetPath, spline: PlanetSpline?, groupHistory: Boolean) {
        val current = planet.paths.find { it.equalPath(path) } ?: throw NoSuchElementException()
        val p = planet.copy(
            paths = planet.paths - current + current.copy(
                spline = spline
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun deletePath(path: PlanetPath, groupHistory: Boolean) {
        val current = planet.paths.find { it.equalPath(path) } ?: throw NoSuchElementException()
        val p = planet.copy(
            paths = planet.paths - current
        )

        setPlanet(p, groupHistory)
    }

    override fun toggleTargetExposure(target: PlanetPoint, exposure: PlanetPoint, groupHistory: Boolean) {
        val current = planet.targets.find { it.point == target } ?: throw NoSuchElementException()
        val p = if (exposure in current.exposure) {
            planet.copy(
                targets = planet.targets - current + current.copy(
                    exposure = current.exposure - exposure
                )
            )
        } else {
            planet.copy(
                targets = planet.targets - current + current.copy(
                    exposure = current.exposure + exposure
                )
            )
        }

        setPlanet(p, groupHistory)
    }

    override fun togglePathExposure(path: PlanetPath, exposure: PlanetPathExposure, groupHistory: Boolean) {
        val current = planet.paths.find { it.equalPath(path) } ?: throw NoSuchElementException()
        val currentExposure = current.exposure.find { it.planetPoint == exposure.planetPoint }

        var newPath = current
        if (currentExposure != null) {
            newPath = current.copy(
                exposure = current.exposure - currentExposure
            )
        }
        if (exposure != currentExposure) {
            newPath = current.copy(
                exposure = current.exposure + exposure
            )
        }

        val p = planet.copy(
            paths = planet.paths - current + newPath
        )
        setPlanet(p, groupHistory)
    }

    override fun togglePathSelect(point: PlanetPoint, direction: PlanetDirection, groupHistory: Boolean) {
        val target = PlanetPathSelect(point, direction)
        val p = when (val current = planet.pathSelects.find { it.point == point }) {
            null -> planet.copy(
                pathSelects = planet.pathSelects + target
            )
            target -> planet.copy(
                pathSelects = planet.pathSelects - current
            )
            else -> planet.copy(
                pathSelects = planet.pathSelects - current + target
            )
        }

        setPlanet(p, groupHistory)
    }

    override fun setStartPoint(point: PlanetPoint, orientation: PlanetDirection, groupHistory: Boolean) {
        val p = planet.copy(
            startPoint = PlanetStartPoint(point, orientation, null)
        )

        setPlanet(p, groupHistory)
    }

    override fun deleteStartPoint(groupHistory: Boolean) {
        val p = planet.copy(
            startPoint = PlanetStartPoint(0L, 0L, PlanetDirection.North, null)
        )

        setPlanet(p, groupHistory)
    }

    override fun setBluePoint(point: PlanetPoint, groupHistory: Boolean) {
        val p = planet.copy(
            bluePoint = point
        )

        setPlanet(p, groupHistory)
    }

    override fun togglePathHiddenState(path: PlanetPath, groupHistory: Boolean) {
        val current = planet.paths.find { it.equalPath(path) } ?: throw NoSuchElementException()
        val p = planet.copy(
            paths = planet.paths - current + current.copy(
                hidden = !current.hidden
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun setPathWeight(path: PlanetPath, weight: Long, groupHistory: Boolean) {
        val pa = planet.paths.find { it.equalPath(path) } ?: throw NoSuchElementException()
        val p = planet.copy(
            paths = planet.paths - pa + pa.copy(
                weight = weight
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun createComment(value: List<String>, position: Vector, groupHistory: Boolean) {
        val p = planet.copy(
            comments = planet.comments + PlanetComment(
                x = position.x,
                y = position.y,
                lines = value,
                alignment = PlanetCommentAlignment.Center
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun setCommentValue(comment: PlanetComment, value: List<String>, groupHistory: Boolean) {
        val p = planet.copy(
            comments = planet.comments - comment + comment.copy(
                lines = value
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun setCommentPosition(comment: PlanetComment, position: Vector, groupHistory: Boolean) {
        val p = planet.copy(
            comments = planet.comments - comment + comment.copy(
                x = position.x,
                y = position.y
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun setCommentAlignment(comment: PlanetComment, alignment: PlanetCommentAlignment, groupHistory: Boolean) {
        val p = planet.copy(
            comments = planet.comments - comment + comment.copy(
                alignment = alignment
            )
        )

        setPlanet(p, groupHistory)
    }

    override fun deleteComment(comment: PlanetComment, groupHistory: Boolean) {
        val p = planet.copy(
            comments = planet.comments - comment
        )

        setPlanet(p, groupHistory)
    }

    override fun translate(delta: PlanetPoint, groupHistory: Boolean) {
        val p = planet.translate(delta)

        setPlanet(p, groupHistory)
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint, groupHistory: Boolean) {
        val p = planet.rotate(direction, origin)

        setPlanet(p, groupHistory)
    }

    override fun scaleWeights(factor: Double, offset: Long, groupHistory: Boolean) {
        val p = planet.scaleWeights(factor, offset)

        setPlanet(p, groupHistory)
    }

    override fun setName(name: String, groupHistory: Boolean) {
        val p = planet.copy(
            name = name
        )

        setPlanet(p, groupHistory)
    }

    override fun undo() {
        planetProperty.undo()
    }

    override fun redo() {
        planetProperty.redo()
    }

    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun parse(content: String): Planet {
            return try {
                json.decodeFromString(Planet.serializer(), content)
            } catch (e: SerializationException) {
                Planet.EMPTY
            }
        }

        fun stringify(planet: Planet): String {
            return try {
                json.encodeToString(Planet.serializer(), planet)
            } catch (e: SerializationException) {
                ""
            }
        }
    }
}
