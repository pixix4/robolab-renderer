package de.robolab.client.renderer.drawable.edit

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.drawable.general.PointAnimatable
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.extraPut
import de.robolab.client.renderer.view.component.GroupView
import de.robolab.client.renderer.view.component.SquareView
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Planet
import kotlin.math.ceil
import kotlin.math.floor

class EditPointEndsManager(
    private val editCallback: IEditCallback,
    private val createPath: CreatePathManager
) {

    val view = GroupView("Edit point ends manager")
    private val map = mutableMapOf<Coordinate, GroupView>()

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet

        updateViews()
    }

    private fun setupViewEvents(coordinate: Coordinate, view: GroupView) {
        for (direction in Direction.values()) {
            view += setupPointEnd(coordinate, direction, editCallback, createPath)
        }
    }

    private fun updateViews() {
        val area = view.document?.plotter?.context?.area ?: return

        val coordinatesToRemove = map.keys.toMutableSet()
        for (x in floor(area.left).toInt()..ceil(area.right).toInt()) {
            for (y in floor(area.top).toInt()..ceil(area.bottom).toInt()) {
                val coordinate = Coordinate(x, y)

                if (coordinate in map) {
                    coordinatesToRemove -= coordinate
                } else {
                    val newView = GroupView("$x, $y")

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
        view.onCanvasResize {
            updateViews()
        }
        view.onViewChange {
            updateViews()
        }
        view.animationTime = 0.0
    }

    companion object {
        fun setupPointEnd(
            coordinate: Coordinate,
            direction: Direction,
            editCallback: IEditCallback?,
            createPath: CreatePathManager?
        ): SquareView {
            val squareView = SquareView(
                coordinate.toPoint() + direction.toVector(PlottingConstraints.POINT_SIZE),
                PlottingConstraints.POINT_SIZE,
                PlottingConstraints.LINE_WIDTH * 0.65,
                ViewColor.TRANSPARENT
            )
            squareView.hoverable = true
            squareView.animationTime = 0.0
            squareView.extraPut(coordinate)
            squareView.extraPut(direction)

            squareView.onPointerDown { event ->
                val callback = editCallback ?: return@onPointerDown

                if (event.altKey) {
                    callback.togglePathSelect(coordinate, direction)
                } else createPath?.startPath(coordinate, direction, event.ctrlKey)

                event.stopPropagation()
            }

            return squareView
        }
    }
}
