package de.robolab.client.renderer.utils

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import kotlin.math.*

interface ITransformation {

    fun canvasToPlanet(canvasCoordinate: Vector) = canvasToPlanet(canvasCoordinate, translation, scale, rotation)

    fun canvasToPlanet(canvasCoordinate: Vector, translation: Vector, scale: Double, rotation: Double): Vector {
        val left = (canvasCoordinate.left - translation.left) / pixelPerUnitDimension.left / scale
        val top = (canvasCoordinate.top - translation.top) / pixelPerUnitDimension.top / scale

        return Vector(
                left * cos(-rotation) - top * sin(-rotation),
                left * sin(-rotation) + top * cos(-rotation)
        )
    }

    fun planetToCanvas(planetCoordinate: Vector) = planetToCanvas(planetCoordinate, translation, scale, rotation)

    fun planetToCanvas(planetCoordinate: Vector, translation: Vector, scale: Double, rotation: Double): Vector {
        val left = (planetCoordinate.left * cos(rotation) - planetCoordinate.top * sin(rotation)) * scale * pixelPerUnitDimension.left + translation.left
        val top = (planetCoordinate.left * sin(rotation) + planetCoordinate.top * cos(rotation)) * scale * pixelPerUnitDimension.top + translation.top
        return Vector(left, top)
    }

    val translationProperty: ObservableValue<Vector>
    val translation: Vector

    val scaleProperty: ObservableValue<Double>
    val scale: Double

    val rotationProperty: ObservableValue<Double>
    val rotation: Double

    val flipViewProperty: ObservableValue<Boolean>
    val flipView: Boolean

    val gridWidth: Double

    val scaledGridWidth: Double
        get() = gridWidth * scale

    val pixelPerUnitDimension: Dimension

    val onViewChange: EventHandler<Unit>

    fun translateBy(point: Vector, duration: Double = 0.0)
    fun translateTo(point: Vector, duration: Double = 0.0)
    fun setTranslation(point: Vector)

    fun rotateBy(angle: Double, center: Vector, duration: Double = 0.0)
    fun rotateTo(angle: Double, center: Vector, duration: Double = 0.0)
    fun setRotationAngle(angle: Double)

    fun scaleBy(factor: Double, center: Vector, duration: Double = 0.0)
    fun scaleTo(scale: Double, center: Vector, duration: Double = 0.0)
    fun scaleIn(center: Vector, duration: Double = 0.0)
    fun scaleOut(center: Vector, duration: Double = 0.0)
    fun resetScale(center: Vector, duration: Double = 0.0)
    fun setScaleFactor(scale: Double)

    fun flip(force: Boolean? = null)

    fun update(msOffset: Double): Boolean

    fun export(): Transformation.State
    fun import(state: Transformation.State)

}
