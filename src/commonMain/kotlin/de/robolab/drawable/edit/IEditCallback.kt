package de.robolab.drawable.edit

import de.robolab.model.Direction
import de.robolab.model.Path

interface IEditCallback {
    fun drawPath(startPoint: Pair<Int, Int>, startDirection: Direction, endPoint: Pair<Int, Int>, endDirection: Direction) {
        println("Plotter action 'drawPath' is not supported!")
    }

    fun deletePath(path: Path) {
        println("Plotter action 'deletePath' is not supported!")
    }

    fun updateControlPoints(path: Path, controlPoints: List<Pair<Double, Double>>, groupHistory: Boolean) {
        println("Plotter action 'updateControlPoints' is not supported!")
    }

    fun toggleTargetSend(sender: Pair<Int, Int>, target: Pair<Int, Int>) {
        println("Plotter action 'toggleTargetSend' is not supported!")
    }

    fun togglePathSend(sender: Pair<Int, Int>, path: Path) {
        println("Plotter action 'togglePathSend' is not supported!")
    }

    fun togglePathSelect(point: Pair<Int, Int>, direction: Direction) {
        println("Plotter action 'togglePathSelect' is not supported!")
    }

    fun undo() {
        println("Plotter action 'undo' is not supported!")
    }

    fun redo() {
        println("Plotter action 'redo' is not supported!")
    }
}
