package de.robolab.client.renderer.utils

import de.robolab.client.renderer.transition.DoubleTransition
import de.robolab.client.renderer.transition.ValueTransition
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Transformation(
    initTranslation: Vector = Vector.ZERO,
    initScale: Double = 1.0,
    initRotation: Double = 0.0,
    override val gridWidth: Double = PIXEL_PER_UNIT,
    pixelPerUnitDimension: Dimension = PIXEL_PER_UNIT_DIMENSION
) : ITransformation {

    override val onViewChange = EventHandler<Unit>()

    override val translationProperty = ValueTransition(initTranslation)
    override val translation by translationProperty

    private var rotationCenter = Vector.ZERO
    override val rotationProperty = DoubleTransition(initRotation)
    override val rotation by rotationProperty

    private var scaleCenter = Vector.ZERO
    override val scaleProperty = DoubleTransition(initScale)
    override val scale by scaleProperty

    override val flipViewProperty = property(false)
    override var flipView by flipViewProperty

    private val internalPixelPerUnitDimension = pixelPerUnitDimension

    private val pixelPerUnitDimensionProperty = flipViewProperty.mapBinding {
        if (it) {
            internalPixelPerUnitDimension * Vector(-1.0, 1.0)
        } else internalPixelPerUnitDimension
    }
    override val pixelPerUnitDimension: Dimension by pixelPerUnitDimensionProperty

    private var hasChanges = false

    override fun translateBy(point: Vector, duration: Double) {
        translateTo(translationProperty.targetValue + point, duration)
    }

    override fun translateTo(point: Vector, duration: Double) {
        translationProperty.animate(point, duration)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    override fun setTranslation(point: Vector) {
        if (point == translation && !translationProperty.isRunning) return
        translationProperty.resetValue(point)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    override fun rotateBy(angle: Double, center: Vector, duration: Double) {
        rotateTo(rotationProperty.targetValue - angle, center, duration)
    }

    override fun rotateTo(angle: Double, center: Vector, duration: Double) {
        rotationCenter = center

        val planetPoint = canvasToPlanet(rotationCenter)
        rotationProperty.animate(angle, duration)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(rotationCenter - newCenter)
    }

    override fun setRotationAngle(angle: Double) {
        if (angle == rotation && !rotationProperty.isRunning) return
        rotationProperty.resetValue((angle - PI) % (2 * PI) + PI)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    override fun scaleBy(factor: Double, center: Vector, duration: Double) {
        scaleTo(scaleProperty.targetValue * factor, center, duration)
    }

    override fun scaleTo(scale: Double, center: Vector, duration: Double) {
        scaleCenter = center

        val planetPoint = canvasToPlanet(scaleCenter)
        scaleProperty.animate(max(min(scale, 10.0), 0.1), duration)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(scaleCenter - newCenter, duration)
    }

    private fun scaleDirected(direction: Int, center: Vector, duration: Double = 0.0) {
        val currentZoomLevel = scale
        var nearestZoomLevel = SCALE_STEPS.minByOrNull { abs(it - currentZoomLevel) } ?: 1.0
        var index = SCALE_STEPS.indexOf(nearestZoomLevel)

        if (direction * currentZoomLevel > direction * nearestZoomLevel) {
            index += direction
            index = max(0, min(index, SCALE_STEPS.lastIndex))
        }
        nearestZoomLevel = SCALE_STEPS[index]

        if (nearestZoomLevel != currentZoomLevel) {
            scaleTo(nearestZoomLevel, center, duration)
        } else {
            index += direction
            index = max(0, min(index, SCALE_STEPS.lastIndex))
            scaleTo(SCALE_STEPS[index], center, duration)
        }
    }

    override fun scaleIn(center: Vector, duration: Double) = scaleDirected(1, center, duration)

    override fun scaleOut(center: Vector, duration: Double) = scaleDirected(-1, center, duration)

    override fun resetScale(center: Vector, duration: Double) {
        scaleTo(1.0, center, duration)
    }

    override fun setScaleFactor(scale: Double) {
        val s = max(min(scale, 10.0), 0.1)
        if (s == this.scale && !scaleProperty.isRunning) return
        scaleProperty.resetValue(s)
        onViewChange.emit(Unit)
        hasChanges = true
    }

    override fun update(msOffset: Double): Boolean {
        var changes = hasChanges
        hasChanges = false

        if (translationProperty.onUpdate(msOffset)) {
            onViewChange.emit(Unit)
            changes = true
        }

        val rotationPlanetPoint = canvasToPlanet(rotationCenter)
        if (rotationProperty.onUpdate(msOffset)) {
            val newCenter = planetToCanvas(rotationPlanetPoint)
            translateBy(rotationCenter - newCenter)
            changes = true
        }

        val scalePlanetPoint = canvasToPlanet(scaleCenter)
        if (scaleProperty.onUpdate(msOffset)) {
            val newCenter = planetToCanvas(scalePlanetPoint)
            translateBy(scaleCenter - newCenter)
            changes = true
        }

        return changes
    }

    override fun flip(force: Boolean?) {
        flipView = force ?: !flipView
    }

    override fun export() = State(translation, scale, rotation, flipView)

    override fun import(state: State) {
        setTranslation(state.translation)
        setScaleFactor(state.scale)
        setRotationAngle(state.rotation)
        flipView = state.flipped
    }

    override fun toString(): String {
        return "Transformation(translation=$translation, rotation=$rotation, scale=$scale, flipView=$flipView)"
    }

    init {
        flipViewProperty.onChange {
            onViewChange.emit()
        }
    }

    companion object {
        const val PIXEL_PER_UNIT: Double = 100.0
        val PIXEL_PER_UNIT_DIMENSION = Dimension(PIXEL_PER_UNIT, -PIXEL_PER_UNIT)


        private val SCALE_STEPS = listOf(
            0.1,
            0.17,
            0.25,
            0.4,
            0.5,
            0.67,
            0.8,
            0.9,
            1.0,
            1.1,
            1.2,
            1.33,
            1.5,
            1.7,
            2.0,
            2.4,
            3.0,
            4.0,
            6.5,
            10.0
        )

        fun normalizeScale(scale: Double) = min(10.0, max(0.1, scale))
    }

    data class State(
        val translation: Vector,
        val scale: Double,
        val rotation: Double,
        val flipped: Boolean
    ) {

        fun isDefault() = this == DEFAULT

        companion object {
            val DEFAULT = State(Vector.ZERO, 1.0, 0.0, false)
        }
    }
}
