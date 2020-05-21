package de.robolab.renderer.drawable.edit

import de.robolab.planet.Coordinate
import de.robolab.planet.Direction
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.document.GroupView
import de.robolab.renderer.document.SquareView
import de.robolab.renderer.document.ViewColor
import de.robolab.renderer.drawable.general.PointAnimatable
import de.robolab.renderer.drawable.utils.toPoint
import de.westermann.kobserve.base.ObservableValue
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class EditPointsManager(
        private val editCallbackProperty: ObservableValue<IEditCallback?>,
        private val createPath: CreatePathManager
) {

    val view = GroupView("Edit points manager")
    private val map = mutableMapOf<Coordinate, SquareView>()

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet

        updateViews()
    }

    private val redColor = ViewColor.POINT_RED.interpolate(ViewColor.SECONDARY_BACKGROUND_COLOR, 0.8)
    private val blueColor = ViewColor.POINT_BLUE.interpolate(ViewColor.SECONDARY_BACKGROUND_COLOR, 0.8)
    private val unknownColor = ViewColor.GRID_TEXT_COLOR.interpolate(ViewColor.SECONDARY_BACKGROUND_COLOR, 0.8)

    private fun setupViewEvents(coordinate: Coordinate, view: SquareView) {
        view.focusable = true

        view.onPointerDown { event ->
            val callback = editCallbackProperty.value ?: return@onPointerDown
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            if (event.ctrlKey && focusedView != null) {
                val targetCoordinate = Coordinate(
                        view.center.left.roundToInt(),
                        view.center.top.roundToInt()
                )

                val exposureCoordinate = Coordinate(
                        focusedView.center.left.roundToInt(),
                        focusedView.center.top.roundToInt()
                )

                callback.toggleTargetExposure(targetCoordinate, exposureCoordinate)

                event.stopPropagation()
            }
        }
        
        for (direction in Direction.values()) {
            view += PointAnimatable.setupPointEnd(coordinate, direction, editCallbackProperty, createPath)
        }
    }

    private fun updateViews() {
        if (editCallbackProperty.value == null) {
            return
        }

        val area = view.document?.plotter?.context?.area ?: return

        val coordinatesToRemove = map.keys.toMutableSet()
        for (x in floor(area.left).toInt()..ceil(area.right).toInt()) {
            for (y in floor(area.top).toInt()..ceil(area.bottom).toInt()) {
                val coordinate = Coordinate(x, y)

                val color = when (coordinate.getColor(planet.bluePoint)) {
                    Coordinate.Color.RED -> redColor
                    Coordinate.Color.BLUE -> blueColor
                    Coordinate.Color.UNKNOWN -> unknownColor
                }

                if (coordinate in map) {
                    val mapView = map[coordinate] ?: continue
                    coordinatesToRemove -= coordinate
                    mapView.setColor(color)
                } else {
                    val newView = SquareView(
                            coordinate.toPoint(),
                            PlottingConstraints.POINT_SIZE,
                            PlottingConstraints.LINE_WIDTH * 0.65,
                            color
                    )

                    setupViewEvents(coordinate, newView)

                    map[coordinate] = newView
                    view += newView
                }
            }
        }

        for (coordinate in coordinatesToRemove) {
            val oldView = map.remove(coordinate) ?: continue
            view -= oldView
        }
    }

    init {
        editCallbackProperty.onChange {
            if (editCallbackProperty.value == null) {
                view.clear()
                map.clear()
            } else {
                updateViews()
            }
        }

        view.onUserTransformation {
            updateViews()
        }
        view.onCanvasResize {
            updateViews()
        }
    }
}