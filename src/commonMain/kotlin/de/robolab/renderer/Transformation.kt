package de.robolab.renderer

import de.robolab.renderer.data.Point
import de.westermann.kobserve.event.EventHandler
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Transformation {

    val onViewChange = EventHandler<Unit>()

    var translation: Point = Point.ZERO
        private set
    var rotation: Double = 0.0
        private set
    var scale: Double = 1.0
        private set

    val scaledGridWidth: Double
        get() = PIXEL_PER_UNIT * scale

    fun translateBy(point: Point) {
        translateTo(translation + point)
    }

    fun translateTo(point: Point) {
        translation = point
        onViewChange.emit(Unit)
    }

    fun rotateBy(angle: Double, center: Point) {
        rotateTo(rotation + angle, center)
    }

    fun rotateTo(angle: Double, center: Point) {
        val planetPoint = canvasToPlanet(center)
        rotation = angle
        val newCenter = planetToCanvas(planetPoint)

        translateBy(center - newCenter)
    }

    fun setRotationAngle(angle: Double) {
        rotation = angle
    }

    fun scaleBy(factor: Double, center: Point) {
        scaleTo(scale * factor, center)
    }

    fun scaleTo(scale: Double, center: Point) {
        val planetPoint = canvasToPlanet(center)
        this.scale = max(min(scale, 40.0), 0.1)
        val newCenter = planetToCanvas(planetPoint)

        translateBy(center - newCenter)
    }

    fun setScaleFactor(scale: Double) {
        this.scale = scale
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
