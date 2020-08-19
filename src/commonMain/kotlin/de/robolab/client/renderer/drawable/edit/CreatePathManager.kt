package de.robolab.client.renderer.drawable.edit

import de.robolab.client.renderer.drawable.general.PathAnimatable.Companion.getControlPointsFromPath
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.extraGet
import de.robolab.client.renderer.view.component.GroupView
import de.robolab.client.renderer.view.component.SplineView
import de.robolab.client.renderer.view.component.SquareView
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.PlanetVersion
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener

class CreatePathManager(
        private val editCallbackProperty: ObservableValue<IEditCallback?>
) {

    val view = GroupView("Create path manager")

    private var data: Data? = null

    fun startPath(coordinate: Coordinate, direction: Direction, drawMode: Boolean) {
        data?.delete()

        data = Data(coordinate, direction, drawMode)
        data?.create()
    }

    init {
        view.animationTime = 0.0
    }

    inner class Data(
        private val startCoordinate: Coordinate,
        private val startDirection: Direction,
        private val drawMode: Boolean
    ) {

        val startPoint = startCoordinate.toPoint() + startDirection.toVector(PlottingConstraints.POINT_SIZE)

        private val splineView = SplineView(
                startPoint,
                startPoint,
                listOf(startPoint, startPoint),
                PlottingConstraints.LINE_WIDTH,
                ViewColor.LINE_COLOR,
                false
        )

        private val controlPoints = mutableListOf<Point>()
        private val listeners = mutableListOf<EventListener<*>>()

        fun delete() {
            for (l in listeners) {
                l.detach()
            }
            listeners.clear()

            view -= splineView
            data = null
        }

        fun create() {
            setupListeners()

            view += splineView
            data = this
        }

        private fun setupListeners() {
            val document = view.document ?: return

            listeners += document.onPointerDrag.reference { event ->
                val hoveredView = document.hoveredStack.lastOrNull() as? SquareView

                val hoveredCoordinate = hoveredView?.extraGet<Coordinate>()
                val hoveredDirection = hoveredView?.extraGet<Direction>()

                if (drawMode) {
                    val lastControlPoint = controlPoints.lastOrNull() ?: startPoint

                    if (event.planetPoint distanceTo lastControlPoint > 0.25) {
                        controlPoints += event.planetPoint
                    }
                }

                if (hoveredCoordinate != null && hoveredDirection != null) {
                    val endPoint = hoveredCoordinate.toPoint() + hoveredDirection.toVector(PlottingConstraints.POINT_SIZE)

                    val cp = if (drawMode) {
                        when (controlPoints.size) {
                            0 -> listOf(startPoint, event.planetPoint)
                            1 -> listOf(startPoint, controlPoints.last(), event.planetPoint)
                            else -> controlPoints
                        }
                    } else {
                        getControlPointsFromPath(
                                PlanetVersion.CURRENT,
                                startCoordinate.toPoint(),
                                startDirection,
                                hoveredCoordinate.toPoint(),
                                hoveredDirection
                        )
                    }

                    splineView.setControlPoints(cp)
                    splineView.setTarget(endPoint)
                } else {
                    val cp = if (drawMode) {
                        when (controlPoints.size) {
                            0 -> listOf(startPoint, event.planetPoint)
                            1 -> listOf(startPoint, controlPoints.last(), event.planetPoint)
                            else -> controlPoints
                        }
                    } else {
                        listOf(startPoint, event.planetPoint)
                    }
                    splineView.setControlPoints(cp)
                    splineView.setTarget(event.planetPoint)
                }

                document.requestRedraw()
                event.stopPropagation()
            }
            listeners += document.onPointerUp.reference { event ->
                val hoveredView = document.hoveredStack.lastOrNull() as? SquareView

                val callback = editCallbackProperty.value
                val hoveredCoordinate = hoveredView?.extraGet<Coordinate>()
                val hoveredDirection = hoveredView?.extraGet<Direction>()


                if (hoveredCoordinate != null && hoveredDirection != null && callback != null) {
                    if (drawMode && controlPoints.isEmpty()) {
                        callback.togglePathSelect(startCoordinate, startDirection)
                    } else {
                        if (drawMode) {
                            val endPoint =
                                hoveredCoordinate.toPoint() + hoveredDirection.toVector(PlottingConstraints.POINT_SIZE)

                            if (controlPoints.last() distanceTo endPoint < 0.25) {
                                controlPoints.removeAt(controlPoints.lastIndex)
                            }
                        }

                        callback.createPath(
                            startCoordinate,
                            startDirection,
                            hoveredCoordinate,
                            hoveredDirection,
                            if (drawMode) controlPoints else emptyList()
                        )
                    }
                }

                delete()

                document.requestRedraw()
                event.stopPropagation()
            }
        }

        init {
            splineView.hoverable = false
        }
    }
}