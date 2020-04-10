package de.robolab.renderer.drawable.edit

import de.robolab.planet.Coordinate
import de.robolab.planet.Direction
import de.robolab.planet.Path
import de.robolab.renderer.data.Point

interface IEditCallback {
    fun createPath(startPoint: Coordinate, startDirection: Direction, endPoint: Coordinate, endDirection: Direction, controlPoints: List<Point>, groupHistory: Boolean = false) {
        println("Plotter action 'drawPath($startPoint, $startDirection, $endPoint, $endDirection, $controlPoints, $groupHistory)' is not supported!")
    }

    fun updatePathControlPoints(path: Path, controlPoints: List<Point>, groupHistory: Boolean = false) {
        println("Plotter action 'updateControlPoints($path, $controlPoints, $groupHistory)' is not supported!")
    }

    fun deletePath(path: Path, groupHistory: Boolean = false) {
        println("Plotter action 'deletePath($path, $groupHistory)' is not supported!")
    }

    fun toggleTargetExposure(target: Coordinate, exposure: Coordinate, groupHistory: Boolean = false) {
        println("Plotter action 'toggleTargetExposure($target, $exposure, $groupHistory)' is not supported!")
    }

    fun togglePathExposure(path: Path, exposure: Coordinate, groupHistory: Boolean = false) {
        println("Plotter action 'togglePathExposure($path, $exposure, $groupHistory)' is not supported!")
    }

    fun togglePathSelect(point: Coordinate, direction: Direction, groupHistory: Boolean = false) {
        println("Plotter action 'togglePathSelect($point, $direction, $groupHistory)' is not supported!")
    }

    fun setStartPoint(point: Coordinate, orientation: Direction, groupHistory: Boolean = false) {
        println("Plotter action 'setStartPoint($point, $orientation, $groupHistory)' is not supported!")
    }

    fun deleteStartPoint(groupHistory: Boolean) {
        println("Plotter action 'deleteStartPoint($groupHistory)' is not supported!")
    }

    fun setBluePoint(point: Coordinate, groupHistory: Boolean = false) {
        println("Plotter action 'setBluePoint($point, $groupHistory)' is not supported!")
    }

    fun togglePathHiddenState(path: Path, groupHistory: Boolean = false) {
        println("Plotter action 'togglePathHiddenState($path, $groupHistory)' is not supported!")
    }
    
    fun setPathWeight(path: Path, weight: Int, groupHistory: Boolean = false) {
        println("Plotter action 'setPathWeight($path, $weight, $groupHistory)' is not supported!")
    }

    fun undo() {
        println("Plotter action 'undo()' is not supported!")
    }

    fun redo() {
        println("Plotter action 'redo()' is not supported!")
    }
}
