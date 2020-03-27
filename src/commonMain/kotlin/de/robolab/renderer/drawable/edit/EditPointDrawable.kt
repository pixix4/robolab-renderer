package de.robolab.renderer.drawable.edit

import de.robolab.model.Coordinate
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.PointerEvent
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class EditPointDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    private val colorTransition = DoubleTransition(0.0)
    private val sizeTransition = DoubleTransition(0.0)
    private val alphaTransition = DoubleTransition(0.0)

    private val transitions = listOf(colorTransition, sizeTransition, alphaTransition)

    private var oldPlanetIsEvenBlue: Boolean? = null
    private var planetIsEvenBlue: Boolean? = null

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for (animatable in transitions) {
            if (animatable.update(ms_offset)) {
                hasChanges = true
            }
        }

        return hasChanges
    }

    override fun onDraw(context: DrawContext) {
        if (sizeTransition.value == 0.0 || alphaTransition.value == 0.0) return

        val size = Point(PlottingConstraints.POINT_SIZE / 2, PlottingConstraints.POINT_SIZE / 2) * sizeTransition.value

        val redColor = context.theme.redColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)
        val blueColor = context.theme.blueColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)
        val greyColor = context.theme.gridTextColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)

        val selectedPoint = editPlanetDrawable.selectedPoint

        for (x in floor(context.area.left).toInt()..ceil(context.area.right).toInt()) {
            for (y in floor(context.area.top).toInt()..ceil(context.area.bottom).toInt()) {
                val isThisPointEven = (x + y) % 2 == 0
                val position = Point(x.toDouble(), y.toDouble())

                val oldColor = oldPlanetIsEvenBlue?.let {
                    if (it == isThisPointEven) {
                        blueColor
                    } else {
                        redColor
                    }
                } ?: greyColor

                val newColor = planetIsEvenBlue?.let {
                    if (it == isThisPointEven) {
                        blueColor
                    } else {
                        redColor
                    }
                } ?: greyColor

                if (selectedPoint?.x == x && selectedPoint.y == y) {
                    val selectedSize = size + Point(PlottingConstraints.HOVER_WIDTH, PlottingConstraints.HOVER_WIDTH)
                    context.fillRect(Rectangle.fromEdges(
                            position - selectedSize,
                            position + selectedSize
                    ), context.theme.highlightColor)
                }

                context.fillRect(Rectangle.fromEdges(
                        position - size,
                        position + size
                ), oldColor.interpolate(newColor, colorTransition.value))
            }
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (!editPlanetDrawable.editable) return emptyList()

        val point = Point(position.left.roundToInt(), position.top.roundToInt())
        if (position.distance(point) < PlottingConstraints.POINT_SIZE / 2) {
            return listOf(point.left.toInt() to point.top.toInt())
        }

        return emptyList()
    }

    fun startExitAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(0.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(0.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
    }

    fun startEnterAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(1.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(1.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
    }

    fun importPlanet(planet: Planet) {
        oldPlanetIsEvenBlue = planetIsEvenBlue
        planetIsEvenBlue = planet.bluePoint?.let {
            (it.x + it.y) % 2 == 0
        }

        colorTransition.resetValue(0.0)
        colorTransition.animate(1.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 4)
    }

    companion object {
        private const val COLOR_OPACITY = 0.85
    }

    override fun onPointerUp(event: PointerEvent): Boolean {
        if (!editPlanetDrawable.editable || event.hasMoved) return false

        val currentPoint = editPlanetDrawable.pointer.findObjectUnderPointer<Coordinate>()
        val currentPath = editPlanetDrawable.pointer.findObjectUnderPointer<Path>()
        val pointSelect = editPlanetDrawable.pointer.findObjectUnderPointer<EditPathSelectDrawable.PointSelect>()

        val selectedPoint = editPlanetDrawable.selectedPoint
        if (selectedPoint != null && (event.ctrlKey || event.altKey)) {
            if (currentPoint != null) {
                editPlanetDrawable.editCallback.toggleTargetExposure(currentPoint, selectedPoint)
            } else if (currentPath != null) {
                editPlanetDrawable.editCallback.togglePathExposure(currentPath, selectedPoint)
            }
            return true
        } else {
            if (pointSelect == null) {
                editPlanetDrawable.selectedPoint = currentPoint
                editPlanetDrawable.selectedPath = if (editPlanetDrawable.selectedPoint == null) {
                    currentPath
                } else {
                    null
                }
            }
        }

        return false
    }
}
