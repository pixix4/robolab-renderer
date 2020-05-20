package de.robolab.renderer.drawable.edit

import de.robolab.planet.Direction
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.document.*
import de.robolab.renderer.document.base.IView
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
        get() = listOf(animatable.view.source) + editableControlPoints + animatable.view.target

    private val lineView = GroupView("Path edit manager - Lines")
    private val controlPointView = GroupView("Path edit manager - Control points")
    
    val view = GroupView(
            "Path edit manager",
            lineView,
            controlPointView
    ).also {
        it.animationTime = 0.0
    }

    fun onUpdate() {
        updateViews()
    }
    
    private fun updateViews() {
        updateControlPoints()
        updateLines()
    }
    
    private fun focusViewInDirection(view: IView, direction: Int) {
        val index = if (view is CircleView) {
            controlPointView.indexOf(view) * 2 + 1
        } else lineView.indexOf(view) * 2
        
        if (index < 0) return
        
        val size = controlPointView.size + lineView.size
        val newIndex =  (index + direction + size) % size

        if (newIndex % 2 == 0) {
            lineView[newIndex / 2].focus()
        } else {
            controlPointView[(newIndex - 1) / 2].focus()
        }
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

    private fun setupControlPointView(view: CircleView) {
        view.focusable = true
        var groupChanges = true
        
        fun updateSize() {
            if (view.isFocused || view.isHovered) {
                view.setRadius(PlottingConstraints.LINE_WIDTH * 3)
            } else {
                view.setRadius(PlottingConstraints.LINE_WIDTH * 2)
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
            val index = controlPointView.indexOf(view)

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
            val index = controlPointView.indexOf(view)
            val cp = editableControlPoints.toMutableList()
            
            when (event.keyCode) {
                KeyCode.DELETE, KeyCode.BACKSPACE -> {
                    cp.removeAt(index)

                    focusViewInDirection(view, -1)
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
                    if (event.shiftKey) {
                        focusViewInDirection(view, -1)
                    } else {
                        focusViewInDirection(view, 1)
                    }
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

        for (index in 0 until max(cpList.size, controlPointView.size)) {
            val cp = cpList.getOrNull(index)
            val v = controlPointView.getOrNull(index) as? CircleView

            if (cp != null && v != null) {
                v.setCenter(cp)
            } else if (cp != null) {
                val newView = CircleView(
                        cp,
                        PlottingConstraints.LINE_WIDTH * 2,
                        ViewColor.EDIT_COLOR
                )
                controlPointView.add(newView)

                setupControlPointView(newView)
            }
        }

        for (i in controlPointView.lastIndex downTo cpList.size) {
            controlPointView.removeAt(i)
        }
    }
    
    private fun setupLineView(view: LineView) {
        view.focusable = true

        fun updateSize() {
            if (view.isHovered || view.isFocused) {
                view.setWidth(PlottingConstraints.LINE_WIDTH)
            } else {
                view.setWidth(PlottingConstraints.LINE_WIDTH / 2.0)
            }
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
        }
        view.onKeyPress {event ->
            val callback = animatable.editProperty.value ?: return@onKeyPress
            val index = lineView.indexOf(view)
            val cp = editableControlPoints.toMutableList()

            when (event.keyCode) {
                KeyCode.SPACE -> {
                    val position = view.source.interpolate(view.target, 0.5)
                    cp.add(index, position)

                    callback.updatePathControlPoints(
                            animatable.reference,
                            cp
                    )

                    focusViewInDirection(view, 1)
                }
                KeyCode.TAB -> {
                    if (event.shiftKey) {
                        focusViewInDirection(view, -1)
                    } else {
                        focusViewInDirection(view, 1)
                    }
                }
                else -> {
                    return@onKeyPress
                }
            }

            event.stopPropagation()
        }
    }

    private fun updateLines() {
        val cpList = linePoints.windowed(2, 1)

        for (index in 0 until max(cpList.size, lineView.size)) {
            val cp = cpList.getOrNull(index)
            val v = lineView.getOrNull(index) as? LineView

            if (cp != null && v != null) {
                v.setSource(cp[0])
                v.setTarget(cp[1])
            } else if (cp != null) {
                val newView = LineView(
                        cp[0], cp[1],
                        PlottingConstraints.LINE_WIDTH / 2.0,
                        ViewColor.EDIT_COLOR
                )
                lineView.add(newView)

                setupLineView(newView)
            }
        }

        for (i in lineView.lastIndex downTo cpList.size) {
            lineView.removeAt(i)
        }
    }

    init {
        updateViews()
    }
}
