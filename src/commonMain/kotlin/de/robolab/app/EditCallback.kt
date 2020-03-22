package de.robolab.app

import de.robolab.drawable.edit.IEditCallback
import de.robolab.model.*
import de.robolab.renderer.History

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
