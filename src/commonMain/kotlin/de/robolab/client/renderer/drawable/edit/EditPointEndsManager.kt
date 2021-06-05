package de.robolab.client.renderer.drawable.edit

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.extraPut
import de.robolab.client.renderer.view.component.GroupView
import de.robolab.client.renderer.view.component.SquareView
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.Planet
import kotlin.math.ceil
import kotlin.math.floor

class EditPointEndsManager(
    private val editCallback: IEditCallback,
    private val createPath: CreatePathManager
) {

    val view = GroupView("Edit point ends manager")
    private val map = mutableMapOf<PlanetPoint, GroupView>()

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet

        updateViews()
    }

    private fun setupViewEvents(coordinate: PlanetPoint, view: GroupView) {
        for (direction in PlanetDirection.values()) {
            view += setupPointEnd(coordinate, direction, editCallback, createPath)
        }
    }

    private fun updateViews() {
        val area = view.document?.plotter?.context?.area ?: return

        val coordinatesToRemove = map.keys.toMutableSet()
        for (x in floor(area.left).toInt()..ceil(area.right).toLong()) {
            for (y in floor(area.top).toInt()..ceil(area.bottom).toLong()) {
                val coordinate = PlanetPoint(x, y)

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
            coordinate: PlanetPoint,
            direction: PlanetDirection,
            editCallback: IEditCallback?,
            createPath: CreatePathManager?
        ): SquareView {
            val squareView = SquareView(
                coordinate.point + direction.toVector(PlottingConstraints.POINT_SIZE),
                PlottingConstraints.POINT_SIZE,
                PlottingConstraints.LINE_WIDTH * 0.65,
                ViewColor.TRANSPARENT
            )
            squareView.hoverable = true
            squareView.animationTime = 0.0
            squareView.extraPut(coordinate)
            squareView.extraPut(direction)

            squareView.registerPointerHint(
                "Toggle path select",
                PointerEvent.Type.DOWN,
                altKey = true
            ) {
                editCallback != null
            }
            squareView.registerPointerHint(
                "Start new path",
                PointerEvent.Type.DOWN,
            ) {
                editCallback != null
            }
            squareView.registerPointerHint(
                "Start new path (draw mode)",
                PointerEvent.Type.DOWN,
                ctrlKey = true
            ) {
                editCallback != null
            }
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
