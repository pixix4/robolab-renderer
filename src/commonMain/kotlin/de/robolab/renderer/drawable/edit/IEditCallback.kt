package de.robolab.renderer.drawable.edit

import de.robolab.model.Coordinate
import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.renderer.data.Point

interface IEditCallback {
    fun drawPath(startPoint: Coordinate, startDirection: Direction, endPoint: Coordinate, endDirection: Direction) {
        println("Plotter action 'drawPath' is not supported!")
    }

    fun deletePath(path: Path) {
        println("Plotter action 'deletePath' is not supported!")
    }

    fun updateControlPoints(path: Path, controlPoints: List<Point>, groupHistory: Boolean) {
        println("Plotter action 'updateControlPoints' is not supported!")
    }

    fun toggleTargetExposure(target: Coordinate, exposure: Coordinate) {
        println("Plotter action 'toggleTargetSend' is not supported!")
    }

    fun togglePathExposure(path: Path, exposure: Coordinate) {
        println("Plotter action 'togglePathSend' is not supported!")
    }

    fun togglePathSelect(point: Coordinate, direction: Direction) {
        println("Plotter action 'togglePathSelect' is not supported!")
    }

    fun undo() {
        println("Plotter action 'undo' is not supported!")
    }

    fun redo() {
        println("Plotter action 'redo' is not supported!")
    }
}
