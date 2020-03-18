package de.robolab.renderer

import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.westermann.kobserve.event.EventHandler
import kotlin.math.*

open class Transformation {

    val onViewChange = EventHandler<Unit>()

    private val translationTransition = ValueTransition(Point.ZERO)
    val translation by translationTransition.valueProperty

    private var rotationCenter = Point.ZERO
    private val rotationTransition = DoubleTransition(0.0)
    val rotation by rotationTransition.valueProperty

    private var scaleCenter = Point.ZERO
    private val scaleTransition = DoubleTransition(1.0)
    val scale by scaleTransition.valueProperty

    val scaledGridWidth: Double
        get() = PIXEL_PER_UNIT * scale

    private var hasChanges = false

    fun translateBy(point: Point, duration: Double = 0.0) {
        translateTo(translationTransition.targetValue + point, duration)
    }

    fun translateTo(point: Point, duration: Double = 0.0) {
        translationTransition.animate(point, duration)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun setTranslation(point: Point) {
        translationTransition.resetValue(point)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun rotateBy(angle: Double, center: Point, duration: Double = 0.0) {
        rotateTo(rotationTransition.targetValue + angle, center, duration)
    }

    fun rotateTo(angle: Double, center: Point, duration: Double = 0.0) {
        rotationCenter = center

        val planetPoint = canvasToPlanet(rotationCenter)
        rotationTransition.animate(angle, duration)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(rotationCenter - newCenter)
    }

    fun setRotationAngle(angle: Double) {
        rotationTransition.resetValue((angle - PI) % (2 * PI) + PI)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun scaleBy(factor: Double, center: Point, duration: Double = 0.0) {
        scaleTo(scaleTransition.targetValue * factor, center, duration)
    }

    fun scaleTo(scale: Double, center: Point, duration: Double = 0.0) {
        scaleCenter = center

        val planetPoint = canvasToPlanet(scaleCenter)
        scaleTransition.animate(max(min(scale, 40.0), 0.1), duration)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(scaleCenter - newCenter)
    }

    fun setScaleFactor(scale: Double) {
        scaleTransition.resetValue(max(min(scale, 40.0), 0.1))
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun update(ms_offset: Double): Boolean {
        var changes = hasChanges
        hasChanges = false

        if (translationTransition.update(ms_offset)) {
            onViewChange.emit(Unit)
            changes = true
        }

        val rotationPlanetPoint = canvasToPlanet(rotationCenter)
        if (rotationTransition.update(ms_offset)) {
            val newCenter = planetToCanvas(rotationPlanetPoint)
            translateBy(rotationCenter - newCenter)
            changes = true
        }

        val scalePlanetPoint = canvasToPlanet(scaleCenter)
        if (scaleTransition.update(ms_offset)) {
            val newCenter = planetToCanvas(scalePlanetPoint)
            translateBy(scaleCenter - newCenter)
            changes = true
        }

        return changes
    }

    fun canvasToPlanet(canvasCoordinate: Point): Point {
        val left = (canvasCoordinate.left - translation.left) / PIXEL_PER_UNIT / scale
        val top = (canvasCoordinate.top - translation.top) / PIXEL_PER_UNIT / -scale

        return Point(
                left * cos(-rotation) - top * sin(-rotation),
                left * sin(-rotation) + top * cos(-rotation)
        )
    }

    fun planetToCanvas(planetCoordinate: Point): Point {
        val left = (planetCoordinate.left * cos(rotation) - planetCoordinate.top * sin(rotation)) * scale * PIXEL_PER_UNIT + translation.left
        val top = (planetCoordinate.left * sin(rotation) + planetCoordinate.top * cos(rotation)) * -scale * PIXEL_PER_UNIT + translation.top
        return Point(left, top)
    }

    companion object {
        const val PIXEL_PER_UNIT: Double = 100.0
    }
}
