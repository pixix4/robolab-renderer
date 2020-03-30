package de.robolab.renderer.utils

import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.westermann.kobserve.event.EventHandler
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class Transformation : ITransformation {

    val onViewChange = EventHandler<Unit>()

    override val translationProperty = ValueTransition(Point.ZERO)
    override val translation by translationProperty

    private var rotationCenter = Point.ZERO
    override val rotationProperty = DoubleTransition(0.0)
    override val rotation by rotationProperty

    private var scaleCenter = Point.ZERO
    override val scaleProperty = DoubleTransition(1.0)
    override val scale by scaleProperty

    override val gridWidth = PIXEL_PER_UNIT

    private var hasChanges = false

    fun translateBy(point: Point, duration: Double = 0.0) {
        translateTo(translationProperty.targetValue + point, duration)
    }

    fun translateTo(point: Point, duration: Double = 0.0) {
        translationProperty.animate(point, duration)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun setTranslation(point: Point) {
        translationProperty.resetValue(point)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun rotateBy(angle: Double, center: Point, duration: Double = 0.0) {
        rotateTo(rotationProperty.targetValue - angle, center, duration)
    }

    fun rotateTo(angle: Double, center: Point, duration: Double = 0.0) {
        rotationCenter = center

        val planetPoint = canvasToPlanet(rotationCenter)
        rotationProperty.animate(angle, duration)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(rotationCenter - newCenter)
    }

    fun setRotationAngle(angle: Double) {
        rotationProperty.resetValue((angle - PI) % (2 * PI) + PI)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun scaleBy(factor: Double, center: Point, duration: Double = 0.0) {
        scaleTo(scaleProperty.targetValue * factor, center, duration)
    }

    fun scaleTo(scale: Double, center: Point, duration: Double = 0.0) {
        scaleCenter = center

        val planetPoint = canvasToPlanet(scaleCenter)
        scaleProperty.animate(max(min(scale, 40.0), 0.1), duration)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(scaleCenter - newCenter)
    }

    fun setScaleFactor(scale: Double) {
        scaleProperty.resetValue(max(min(scale, 40.0), 0.1))
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun update(ms_offset: Double): Boolean {
        var changes = hasChanges
        hasChanges = false

        if (translationProperty.update(ms_offset)) {
            onViewChange.emit(Unit)
            changes = true
        }

        val rotationPlanetPoint = canvasToPlanet(rotationCenter)
        if (rotationProperty.update(ms_offset)) {
            val newCenter = planetToCanvas(rotationPlanetPoint)
            translateBy(rotationCenter - newCenter)
            changes = true
        }

        val scalePlanetPoint = canvasToPlanet(scaleCenter)
        if (scaleProperty.update(ms_offset)) {
            val newCenter = planetToCanvas(scalePlanetPoint)
            translateBy(scaleCenter - newCenter)
            changes = true
        }

        return changes
    }

    companion object {
        const val PIXEL_PER_UNIT: Double = 100.0
    }
}
