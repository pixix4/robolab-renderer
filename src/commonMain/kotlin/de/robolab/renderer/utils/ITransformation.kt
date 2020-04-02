package de.robolab.renderer.utils

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.westermann.kobserve.ReadOnlyProperty
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

    val translationProperty: ReadOnlyProperty<Point>
    val translation: Point

    val scaleProperty: ReadOnlyProperty<Double>
    val scale: Double

    val rotationProperty: ReadOnlyProperty<Double>
    val rotation: Double

    val gridWidth: Double

    val scaledGridWidth: Double
        get() = gridWidth * scale

    val pixelPerUnitDimension: Dimension
}