package de.robolab.renderer.drawable.edit

import de.robolab.planet.Direction
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.document.CircleView
import de.robolab.renderer.document.MultiLineView
import de.robolab.renderer.document.ViewColor
import de.robolab.renderer.drawable.general.PathAnimatable
import de.robolab.renderer.platform.KeyCode
import kotlin.math.max
import kotlin.math.round

class PathEditManager(
        private val animatable: PathAnimatable
) {

    private val controlPoints
        get() = animatable.view.controlPoints

    private val editableControlPoints
        get() = controlPoints.drop(1).dropLast(if (animatable.isOneWayPath) 0 else 1)

    private val linePoints
        get() = listOf(animatable.view.source) + controlPoints + animatable.view.target

    val view = MultiLineView(linePoints, PlottingConstraints.LINE_WIDTH / 2, ViewColor.EDIT_COLOR)

    val controlPointViews = mutableListOf<CircleView>()

    fun onUpdate() {
        view.setPoints(linePoints, 0.0)
        updateControlPoints()
        view.requestRedraw()
    }
    
    private fun updateControlPoint(index: Int, position: Point): Point {
        return when {
            index == 0 -> {
                val basisVector = animatable.reference.sourceDirection.toVector()
                val referenceVector = position - controlPoints.first()

                val (distance, targetVector) = referenceVector projectOnto basisVector

                if (distance > PlottingConstraints.CURVE_FIRST_POINT) {
                    controlPoints.first() + targetVector
                } else {
                    controlPoints.first() + basisVector * PlottingConstraints.CURVE_FIRST_POINT
                }
            }
            index == editableControlPoints.lastIndex && !animatable.isOneWayPath -> {
                val basisVector = animatable.reference.targetDirection.toVector()
                val referenceVector = position - controlPoints.last()

                val (distance, targetVector) = referenceVector projectOnto basisVector

                if (distance > PlottingConstraints.CURVE_FIRST_POINT) {
                    controlPoints.last() + targetVector
                } else {
                    controlPoints.last() + basisVector * PlottingConstraints.CURVE_FIRST_POINT
                }
            }
            else -> position
        }.let {
            Point(
                    round(it.left * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR,
                    round(it.top * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            )
        }
    }

    private fun setupPointView(view: CircleView) {
        view.focusable = true
        var groupChanges = true
        
        fun updateSize() {
            if (view.isFocused || view.isHovered) {
                view.setRadius(PlottingConstraints.LINE_WIDTH * 3, 0.0)
            } else {
                view.setRadius(PlottingConstraints.LINE_WIDTH * 2, 0.0)
            }
            groupChanges = false
        }

        view.onHoverEnter {
            updateSize()
        }
        view.onHoverLeave  {
            updateSize()
        }
        view.onFocus {
            updateSize()
        }
        view.onBlur  {
            updateSize()
        }

        view.onPointerDown { event ->
            event.stopPropagation()
            groupChanges = false
        }
        view.onPointerDrag { event ->
            event.stopPropagation()

            if (!view.isFocused) {
                view.focus()
            }

            val callback = animatable.editProperty.value ?: return@onPointerDrag
            val index = controlPointViews.indexOf(view)

            val cp = editableControlPoints.toMutableList()

            cp[index] = updateControlPoint(index, event.canvasPoint)

            if (cp != editableControlPoints) {
                callback.updatePathControlPoints(
                        animatable.reference,
                        cp,
                        groupChanges
                )
                groupChanges = true
            }
        }
        view.onPointerUp { event ->
            event.stopPropagation()
            groupChanges = false
        }
        view.onPointerSecondaryAction { event ->
            event.stopPropagation()
            // TODO
        }
        view.onKeyPress {event ->
            val callback = animatable.editProperty.value ?: return@onKeyPress
            val index = controlPointViews.indexOf(view)
            val cp = editableControlPoints.toMutableList()
            
            when (event.keyCode) {
                KeyCode.DELETE, KeyCode.BACKSPACE -> {
                    cp.removeAt(index)

                    callback.updatePathControlPoints(
                            animatable.reference,
                            cp
                    )
                    groupChanges = false
                }
                KeyCode.ARROW_LEFT -> {
                    cp[index] = updateControlPoint(
                            index,
                            cp[index] + Direction.WEST.toVector() * PlottingConstraints.PRECISION
                    )

                    if (cp != editableControlPoints) {
                        callback.updatePathControlPoints(animatable.reference, cp, groupChanges)
                        groupChanges = true
                    }
                }
                KeyCode.ARROW_RIGHT -> {
                    cp[index] = updateControlPoint(
                            index,
                            cp[index] + Direction.EAST.toVector() * PlottingConstraints.PRECISION
                    )

                    if (cp != editableControlPoints) {
                        callback.updatePathControlPoints(animatable.reference, cp, groupChanges)
                        groupChanges = true
                    }
                }
                KeyCode.ARROW_UP -> {
                    cp[index] = updateControlPoint(
                            index,
                            cp[index] + Direction.NORTH.toVector() * PlottingConstraints.PRECISION
                    )

                    if (cp != editableControlPoints) {
                        callback.updatePathControlPoints(animatable.reference, cp, groupChanges)
                        groupChanges = true
                    }
                }
                KeyCode.ARROW_DOWN -> {
                    cp[index] = updateControlPoint(
                            index,
                            cp[index] + Direction.SOUTH.toVector() * PlottingConstraints.PRECISION
                    )

                    if (cp != editableControlPoints) {
                        callback.updatePathControlPoints(animatable.reference, cp, groupChanges)
                        groupChanges = true
                    }
                }
                KeyCode.TAB -> {
                    var i = index
                    if (event.shiftKey) {
                        i -=  1
                        if (i < 0) i = controlPointViews.lastIndex
                    } else {
                        i += 1
                        if (i > controlPointViews.lastIndex) i = 0
                    }
                    controlPointViews[i].focus()
                }
                else -> {
                    groupChanges = false
                    return@onKeyPress
                }
            }

            event.stopPropagation()
        }
    }

    private fun updateControlPoints() {
        val cpList = editableControlPoints

        for (index in 0 until max(cpList.size, controlPointViews.size)) {
            val cp = cpList.getOrNull(index)
            val v = controlPointViews.getOrNull(index)

            if (cp != null && v != null) {
                v.setCenter(cp, 0.0)
            } else if (cp != null) {
                val newView = CircleView(
                        cp,
                        PlottingConstraints.LINE_WIDTH * 2,
                        ViewColor.EDIT_COLOR
                )
                controlPointViews += newView
                view += newView

                setupPointView(newView)
            }
        }

        while (controlPointViews.size > cpList.size) {
            val v = controlPointViews.removeAt(controlPointViews.lastIndex)
            view -= v
        }
    }

    init {
        updateControlPoints()
    }
}
