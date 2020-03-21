package de.robolab.jfx

import de.robolab.drawable.edit.EditPlanetDrawable
import de.robolab.drawable.edit.IEditCallback
import de.robolab.jfx.adapter.FxCanvas
import de.robolab.jfx.adapter.FxTimer
import de.robolab.jfx.adapter.toProperty
import de.robolab.model.*
import de.robolab.model.Target
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.History
import de.westermann.kobserve.property.mapBinding
import javafx.application.Platform
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import tornadofx.*
import java.time.LocalDate
import kotlin.system.exitProcess

class MainView : View() {

    class EditCallback(private val history: History<Planet>) : IEditCallback {
        private var planet by history.valueProperty

        private var lastUpdateControlPoints: Pair<Path, List<Pair<Double, Double>>>? = null
        override fun drawPath(startPoint: Pair<Int, Int>, startDirection: Direction, endPoint: Pair<Int, Int>, endDirection: Direction) {
            lastUpdateControlPoints = null
            planet = planet.copy(
                    pathList = planet.pathList + Path(
                            startPoint, startDirection,
                            endPoint, endDirection,
                            1
                    )
            )
        }

        override fun deletePath(path: Path) {
            lastUpdateControlPoints = null
            val pathList = planet.pathList - path

            planet = planet.copy(pathList = pathList)
        }

        override fun updateControlPoints(path: Path, controlPoints: List<Pair<Double, Double>>, groupHistory: Boolean) {
            val newPath = path.copy(controlPoints = controlPoints)
            val pathList = planet.pathList - path + newPath
            val newPlanet = planet.copy(pathList = pathList)

            val lastUpdate = lastUpdateControlPoints
            if (groupHistory && lastUpdate != null && lastUpdate.first.equalPath(path) && lastUpdate.second.size == controlPoints.size) {
                history.replace(newPlanet)
            } else {
                planet = newPlanet
            }
            lastUpdateControlPoints = path to controlPoints
        }

        override fun toggleTargetSend(sender: Pair<Int, Int>, target: Pair<Int, Int>) {
            lastUpdateControlPoints = null
            val currentTargets = planet.targetList.toMutableList()
            val t = currentTargets.find { it.target == target }
            if (t == null) {
                currentTargets += Target(target, setOf(sender))
            } else {
                currentTargets.remove(t)

                if (sender in t.exposure) {
                    if (t.exposure.size > 1) {
                        currentTargets += t.copy(
                                exposure = t.exposure - sender
                        )
                    }
                } else {
                    currentTargets += t.copy(
                            exposure = t.exposure + sender
                    )
                }
            }
            planet = planet.copy(targetList = currentTargets)
        }

        override fun togglePathSend(sender: Pair<Int, Int>, path: Path) {
            lastUpdateControlPoints = null
            val currentPaths = planet.pathList.toMutableList()
            val p = currentPaths.find { it.equalPath(path) } ?: return

            currentPaths.remove(p)

            if (sender in p.exposure) {
                currentPaths += p.copy(
                        exposure = p.exposure - sender
                )

            } else {
                currentPaths += p.copy(
                        exposure = p.exposure + sender
                )
            }

            planet = planet.copy(pathList = currentPaths)
        }

        override fun togglePathSelect(point: Pair<Int, Int>, direction: Direction) {
            lastUpdateControlPoints = null
            val currentPathSelects = planet.pathSelectList.toMutableList()
            val p = currentPathSelects.find { it.point == point }

            currentPathSelects.remove(p)

            if (direction != p?.direction) {
                currentPathSelects += PathSelect(point, direction)
            }

            planet = planet.copy(pathSelectList = currentPathSelects)
        }

        override fun undo() {
            lastUpdateControlPoints = null
            history.undo()
        }

        override fun redo() {
            lastUpdateControlPoints = null
            history.redo()
        }
    }

    override val root: BorderPane = borderpane {
        title = headerText

        Platform.runLater {
            requestFocus()
        }

        val canvas = FxCanvas()
        val timer = FxTimer(50.0)
        val plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)

        val planetDrawable = EditPlanetDrawable()
        plotter.drawable = planetDrawable

        val history = History(PLANET_1)
        history.push(PLANET_2)
        history.push(PLANET_3)
        history.push(PLANET_4)
        history.push(PLANET_5)

        planetDrawable.editCallback = EditCallback(history)

        var isUndoPhase = false

        val anim = FxTimer(1000 / (ANIMATION_TIME * 1.25))
        anim.onRender {
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

        center {
            vbox {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                toolbar {
                    val toggleGroup = ToggleGroup()
                    togglebutton("Animate", toggleGroup, false) {
                        selectedProperty().onChange {
                            if (it) {
                                anim.start()
                            } else {
                                anim.stop()
                            }
                        }
                    }
                    togglebutton("Editable", toggleGroup, false) {
                        selectedProperty().toProperty().bindBidirectional(planetDrawable.editableProperty)
                    }
                }

                hbox {
                    vgrow = Priority.ALWAYS
                    hgrow = Priority.ALWAYS

                    add(canvas.canvas)
                    canvas.canvas.widthProperty().bind(widthProperty())
                    canvas.canvas.heightProperty().bind(heightProperty())
                }

                hbox {
                    label {
                        textProperty().toProperty().bind(plotter.pointerProperty.mapBinding {
                            it.roundedPosition.toString() + " | " + it.objectsUnderPointer
                        })
                    }
                }
            }
        }
    }

    override fun onUndock() {
        exitProcess(0)
    }

    companion object {
        val headerText: String = "RoboLab ${LocalDate.now().year}"

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
