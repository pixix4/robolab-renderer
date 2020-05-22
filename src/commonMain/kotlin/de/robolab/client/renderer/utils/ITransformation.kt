package de.robolab.client.renderer.utils

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import kotlin.math.cos
import kotlin.math.sin

interface ITransformation {

    fun canvasToPlanet(canvasCoordinate: Point) = canvasToPlanet(canvasCoordinate, translation, scale, rotation)

    fun canvasToPlanet(canvasCoordinate: Point, translation: Point, scale: Double, rotation: Double): Point {
        val left = (canvasCoordinate.left - translation.left) / pixelPerUnitDimension.left / scale
        val top = (canvasCoordinate.top - translation.top) / pixelPerUnitDimension.top / scale

        return Point(
                left * cos(-rotation) - top * sin(-rotation),
                left * sin(-rotation) + top * cos(-rotation)
        )
    }

    fun planetToCanvas(planetCoordinate: Point) = planetToCanvas(planetCoordinate, translation, scale, rotation)

    fun planetToCanvas(planetCoordinate: Point, translation: Point, scale: Double, rotation: Double): Point {
        val left = (planetCoordinate.left * cos(rotation) - planetCoordinate.top * sin(rotation)) * scale * pixelPerUnitDimension.left + translation.left
        val top = (planetCoordinate.left * sin(rotation) + planetCoordinate.top * cos(rotation)) * scale * pixelPerUnitDimension.top + translation.top
        return Point(left, top)
    }

    val translationProperty: ObservableValue<Point>
    val translation: Point

    val scaleProperty: ObservableValue<Double>
    val scale: Double

    val rotationProperty: ObservableValue<Double>
    val rotation: Double

    val gridWidth: Double

    val scaledGridWidth: Double
        get() = gridWidth * scale

    val pixelPerUnitDimension: Dimension
}