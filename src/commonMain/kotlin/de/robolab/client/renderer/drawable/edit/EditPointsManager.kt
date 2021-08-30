package de.robolab.client.renderer.drawable.edit

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.GroupView
import de.robolab.client.renderer.view.component.SquareView
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.Planet
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.roundToLong

class EditPointsManager(
    private val editCallback: IEditCallback,
) {

    val view = GroupView("Edit points manager")
    private val map = mutableMapOf<PlanetPoint, SquareView>()

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet

        updateViews()
    }

    private val redColor = ViewColor.POINT_RED.interpolate(ViewColor.SECONDARY_BACKGROUND_COLOR, 0.8)
    private val blueColor = ViewColor.POINT_BLUE.interpolate(ViewColor.SECONDARY_BACKGROUND_COLOR, 0.8)
    private val unknownColor = ViewColor.GRID_TEXT_COLOR.interpolate(ViewColor.SECONDARY_BACKGROUND_COLOR, 0.8)

    private fun setupViewEvents(coordinate: PlanetPoint, view: SquareView) {
        view.focusable = true

        view.registerPointerHint(
            {
                val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
                if (focusedView != null) {
                    val exposureCoordinate = PlanetPoint(
                        focusedView.center.left.roundToLong(),
                        focusedView.center.top.roundToLong()
                    )
                    "Toggle target (Exposure: ${exposureCoordinate})"
                } else {
                    "Toggle target"
                }
            },
            PointerEvent.Type.DOWN,
            ctrlKey = true
        ) {
            view.document?.focusedStack?.lastOrNull() as? SquareView != null
        }
        view.onPointerDown { event ->
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            if (event.ctrlKey && focusedView != null) {
                val targetCoordinate = PlanetPoint(
                        view.center.left.roundToLong(),
                        view.center.top.roundToLong()
                )

                val exposureCoordinate = PlanetPoint(
                        focusedView.center.left.roundToLong(),
                        focusedView.center.top.roundToLong()
                )

                editCallback.toggleTargetExposure(targetCoordinate, exposureCoordinate)

                event.stopPropagation()
            }
        }

        view.onPointerSecondaryAction { event ->
            event.stopPropagation()

            val startPoint = planet.startPoint.point

            view.menu(event, "Point ${coordinate.x}, ${coordinate.y}") {
                if (startPoint != coordinate) {
                    action("Translate start point") {
                        editCallback.translate(
                            PlanetPoint(
                                coordinate.x - startPoint.x,
                                coordinate.y - startPoint.y
                            )
                        )
                    }
                }

                action("Rotate clockwise") {
                    editCallback.rotate(Planet.RotateDirection.CLOCKWISE, coordinate)
                }
                action("Rotate counter clockwise") {
                    editCallback.rotate(Planet.RotateDirection.COUNTER_CLOCKWISE, coordinate)
                }
            }
        }
    }

    private fun updateViews() {
        val area = view.document?.plotter?.context?.area ?: return

        val coordinatesToRemove = map.keys.toMutableSet()
        for (x in floor(area.left).toInt()..ceil(area.right).toLong()) {
            for (y in floor(area.top).toInt()..ceil(area.bottom).toLong()) {
                val coordinate = PlanetPoint(x, y)

                val color = when (coordinate.getColor(planet.bluePoint)) {
                    PlanetPoint.Color.Red -> redColor
                    PlanetPoint.Color.Blue -> blueColor
                    PlanetPoint.Color.Unknown -> unknownColor
                }

                if (coordinate in map) {
                    val mapView = map[coordinate] ?: continue
                    coordinatesToRemove -= coordinate
                    mapView.setColor(color)
                } else {
                    val newView = SquareView(
                            coordinate.point,
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
        view.onCanvasResize {
            updateViews()
        }
        view.onViewChange {
            updateViews()
        }
        view.animationTime = 0.0
    }
}
