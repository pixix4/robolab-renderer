package de.robolab.app

import de.robolab.model.*
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.History
import de.robolab.renderer.drawable.edit.EditPlanetDrawable
import de.robolab.renderer.platform.CommonTimer
import de.robolab.renderer.platform.ICanvas
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class Main(val canvas: ICanvas) {

    private val timer = CommonTimer(50.0)
    private val plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)
    private val planetDrawable = EditPlanetDrawable()

    private val animationTimer = CommonTimer(1000 / (ANIMATION_TIME * 1.25))

    val animateProperty = property(false)
    val editableProperty = planetDrawable.editableProperty
    val pointerProperty = plotter.pointerProperty.mapBinding {
        it.roundedPosition.toString() + " | " + it.objectsUnderPointer
    }

    init {
        plotter.drawable = planetDrawable

        animateProperty.onChange {
            if (animateProperty.value) {
                editableProperty.value = false
            }

            if (animateProperty.value) {
                animationTimer.start()
            } else {
                animationTimer.stop()
            }
        }
        editableProperty.onChange {
            if (editableProperty.value) {
                animateProperty.value = false
            }
        }

        val history = History(PLANET_1)
        history.push(PLANET_2)
        history.push(PLANET_3)
        history.push(PLANET_4)
        history.push(PLANET_5)

        planetDrawable.editCallback = EditCallback(history)

        var isUndoPhase = false

        animationTimer.onRender {
            if (isUndoPhase && !history.canUndo) {
                isUndoPhase = false
            } else if (!isUndoPhase && !history.canRedo) {
                isUndoPhase = true
            }
            if (isUndoPhase) {
                history.undo()
            } else {
                history.redo()
            }
        }

        history.valueProperty.onChange {
            planetDrawable.importPlanet(history.value)
        }

        history.undo()
    }

    companion object {

        const val ANIMATION_TIME = 1000.0

        val PLANET_1 = Planet(
                0 to 0,
                false,
                listOf(
                        Path(
                                0 to 0,
                                Direction.NORTH,
                                0 to 1,
                                Direction.SOUTH,
                                4
                        )
                )
        )

        val PLANET_2 = Planet(
                0 to 0,
                false,
                listOf(
                        Path(
                                0 to 0,
                                Direction.NORTH,
                                0 to 1,
                                Direction.SOUTH,
                                4
                        ),
                        Path(
                                0 to 1,
                                Direction.NORTH,
                                0 to 2,
                                Direction.SOUTH,
                                2
                        )
                ),
                listOf(
                        Target(
                                -1 to 3,
                                setOf(0 to 0)
                        )
                )
        )

        val PLANET_3 = Planet(
                0 to 0,
                false,
                listOf(
                        Path(
                                0 to 0,
                                Direction.NORTH,
                                0 to 1,
                                Direction.SOUTH,
                                4
                        ),
                        Path(
                                0 to 1,
                                Direction.NORTH,
                                0 to 2,
                                Direction.SOUTH,
                                2
                        ),
                        Path(
                                0 to 2,
                                Direction.EAST,
                                1 to 2,
                                Direction.WEST,
                                1
                        )
                ),
                listOf(
                        Target(
                                -1 to 3,
                                setOf(0 to 0)
                        )
                ),
                listOf(
                        PathSelect(
                                0 to 2,
                                Direction.NORTH
                        )
                )
        )
        val PLANET_4 = Planet(
                0 to 0,
                false,
                listOf(
                        Path(
                                0 to 0,
                                Direction.NORTH,
                                0 to 1,
                                Direction.SOUTH,
                                4
                        ),
                        Path(
                                0 to 1,
                                Direction.NORTH,
                                0 to 2,
                                Direction.SOUTH,
                                2
                        ),
                        Path(
                                0 to 2,
                                Direction.EAST,
                                1 to 2,
                                Direction.WEST,
                                1
                        ),
                        Path(
                                1 to 2,
                                Direction.SOUTH,
                                0 to 1,
                                Direction.EAST,
                                4
                        )
                ),
                listOf(
                        Target(
                                -1 to 3,
                                setOf(0 to 0)
                        )
                ),
                listOf(
                        PathSelect(
                                0 to 2,
                                Direction.NORTH
                        )
                )
        )

        val PLANET_5 = Planet(
                0 to 0,
                false,
                listOf(
                        Path(
                                0 to 0,
                                Direction.NORTH,
                                0 to 1,
                                Direction.SOUTH,
                                4
                        ),
                        Path(
                                0 to 1,
                                Direction.NORTH,
                                0 to 2,
                                Direction.SOUTH,
                                2
                        ),
                        Path(
                                0 to 2,
                                Direction.EAST,
                                1 to 2,
                                Direction.WEST,
                                1
                        ),
                        Path(
                                1 to 2,
                                Direction.SOUTH,
                                0 to 1,
                                Direction.EAST,
                                4
                        ),
                        Path(
                                0 to 0,
                                Direction.WEST,
                                0 to 1,
                                Direction.WEST,
                                1
                        ),
                        Path(
                                0 to 1,
                                Direction.WEST,
                                0 to 2,
                                Direction.WEST,
                                4
                        )
                ),
                pathSelectList = listOf(
                        PathSelect(
                                0 to 2,
                                Direction.NORTH
                        )
                )
        )
    }
}