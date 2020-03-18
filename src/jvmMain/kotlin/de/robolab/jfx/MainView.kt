package de.robolab.jfx

import de.robolab.drawable.EditPlanetDrawable
import de.robolab.drawable.PlanetDrawable
import de.robolab.jfx.adapter.FxCanvas
import de.robolab.jfx.adapter.FxTimer
import de.robolab.model.*
import de.robolab.model.Target
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.History
import javafx.application.Platform
import javafx.scene.layout.BorderPane
import tornadofx.*
import java.time.LocalDate
import kotlin.system.exitProcess

/**
 * @author leon
 */
class MainView : View() {

    override val root: BorderPane = borderpane {
        title = headerText

        Platform.runLater {
            requestFocus()
        }

        val canvas = FxCanvas()
        val timer = FxTimer(50.0)
        val plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)

        center {
            add(canvas.canvas)
            canvas.canvas.widthProperty().bind(widthProperty())
            canvas.canvas.heightProperty().bind(heightProperty())
        }

        // initAnimatedPlanet(plotter)
        initEditPlanet(plotter)
    }

    private fun initAnimatedPlanet(plotter: DefaultPlotter) {
        val planetDrawable = PlanetDrawable()
        plotter.drawable = planetDrawable

        val planetList = listOf(PLANET_1, PLANET_2, PLANET_3, PLANET_4, PLANET_5)
        var planetIndex = 0

        val anim = FxTimer(1000 / (ANIMATION_TIME * 1.25))
        anim.start()
        anim.onRender {
            println("Render planet index $planetIndex")
            planetDrawable.importPlanet(planetList[planetIndex])
            planetIndex = (planetIndex + 1) % planetList.size
        }
    }

    private fun initEditPlanet(plotter: DefaultPlotter) {
        val planetHistory = History(PLANET_4)
        var planet by planetHistory.valueProperty

        val planetDrawable = EditPlanetDrawable()

        planetDrawable.editCallback = object : EditPlanetDrawable.IEditCallback {
            var lastUpdateControlPoints: Pair<Path, List<Pair<Double, Double>>>? = null
            override fun onDrawPath(startPoint: Pair<Int, Int>, startDirection: Direction, endPoint: Pair<Int, Int>, endDirection: Direction) {
                lastUpdateControlPoints = null
                planet = planet.copy(
                        pathList = planet.pathList + Path(
                                startPoint, startDirection,
                                endPoint, endDirection,
                                1
                        )
                )
            }

            override fun onDeletePath(path: Path) {
                lastUpdateControlPoints = null
                val pathList = planet.pathList - path

                planet = planet.copy(pathList = pathList)
            }

            override fun onUpdateControlPoints(path: Path, controlPoints: List<Pair<Double, Double>>) {
                val newPath = path.copy(controlPoints = controlPoints)
                val pathList = planet.pathList - path + newPath
                val newPlanet = planet.copy(pathList = pathList)

                val lastUpdate = lastUpdateControlPoints
                if (lastUpdate != null && lastUpdate.first.equalPath(path) && lastUpdate.second.size == controlPoints.size) {
                    planetHistory.replace(newPlanet)
                }else {
                    planet = newPlanet
                }
                lastUpdateControlPoints = path to controlPoints
            }

            override fun undo() {
                lastUpdateControlPoints = null
                planetHistory.undo()
            }

            override fun redo() {
                lastUpdateControlPoints = null
                planetHistory.redo()
            }
        }

        plotter.drawable = planetDrawable

        planetHistory.valueProperty.onChange {
            planetDrawable.importPlanet(planet)
        }

        planetDrawable.importPlanet(planet)
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
