package de.robolab.client.renderer.utils

import de.robolab.client.renderer.transition.DoubleTransition
import de.robolab.client.renderer.transition.ValueTransition
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Transformation(
    initTranslation: Point = Point.ZERO,
    initScale: Double = 1.0,
    initRotation: Double = 0.0,
    override val gridWidth: Double = PIXEL_PER_UNIT,
    pixelPerUnitDimension: Dimension = PIXEL_PER_UNIT_DIMENSION
) : ITransformation {

    val onViewChange = EventHandler<Unit>()

    override val translationProperty = ValueTransition(initTranslation)
    override val translation by translationProperty

    private var rotationCenter = Point.ZERO
    override val rotationProperty = DoubleTransition(initRotation)
    override val rotation by rotationProperty

    private var scaleCenter = Point.ZERO
    override val scaleProperty = DoubleTransition(initScale)
    override val scale by scaleProperty

    val flipViewProperty = property(false)
    var flipView by flipViewProperty

    private val internalPixelPerUnitDimension = pixelPerUnitDimension

    private val pixelPerUnitDimensionProperty = flipViewProperty.mapBinding {
        if (it) {
            internalPixelPerUnitDimension * Point(-1.0, 1.0)
        } else internalPixelPerUnitDimension
    }
    override val pixelPerUnitDimension: Dimension by pixelPerUnitDimensionProperty

    private var hasChanges = false

    override fun canvasToPlanet(canvasCoordinate: Point): Point {
        return super.canvasToPlanet(
            canvasCoordinate,
            translationProperty.targetValue,
            scaleProperty.targetValue,
            rotationProperty.targetValue
        )
    }

    override fun planetToCanvas(planetCoordinate: Point): Point {
        return super.planetToCanvas(
            planetCoordinate,
            translationProperty.targetValue,
            scaleProperty.targetValue,
            rotationProperty.targetValue
        )
    }

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
        scaleProperty.animate(max(min(scale, 10.0), 0.1), duration)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(scaleCenter - newCenter, duration)
    }

    private fun scaleDirected(direction: Int, center: Point, duration: Double = 0.0) {
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

    fun scaleIn(center: Point, duration: Double = 0.0) = scaleDirected(1, center, duration)

    fun scaleOut(center: Point, duration: Double = 0.0) = scaleDirected(-1, center, duration)

    fun resetScale(center: Point, duration: Double = 0.0) {
        scaleTo(1.0, center, duration)
    }

    fun setScaleFactor(scale: Double) {
        scaleProperty.resetValue(max(min(scale, 10.0), 0.1))
        onViewChange.emit(Unit)
        hasChanges = true
    }

    fun update(msOffset: Double): Boolean {
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

    fun export() = State(translation, scale, rotation, flipViewProperty.value)

    fun import(state: State) {
        setTranslation(state.translation)
        setScaleFactor(state.scale)
        setRotationAngle(state.rotation)
        flipViewProperty.value = state.flipped
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
        val translation: Point,
        val scale: Double,
        val rotation: Double,
        val flipped: Boolean
    ) {

        fun isDefault() = this == DEFAULT

        companion object {
            val DEFAULT = State(Point.ZERO, 1.0, 0.0, false)
        }
    }
}
