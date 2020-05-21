package de.robolab.renderer.drawable.edit

import de.robolab.planet.Comment
import de.robolab.planet.Coordinate
import de.robolab.planet.Direction
import de.robolab.planet.Path
import de.robolab.renderer.data.Point
import de.robolab.utils.Logger

private val logger = Logger("IEditCallback")

interface IEditCallback {
    fun createPath(startPoint: Coordinate, startDirection: Direction, endPoint: Coordinate, endDirection: Direction, controlPoints: List<Point>, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'drawPath($startPoint, $startDirection, $endPoint, $endDirection, $controlPoints, $groupHistory)' is not supported!" }
    }

    fun updatePathControlPoints(path: Path, controlPoints: List<Point>, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'updateControlPoints($path, $controlPoints, $groupHistory)' is not supported!" }
    }

    fun deletePath(path: Path, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'deletePath($path, $groupHistory)' is not supported!" }
    }

    fun toggleTargetExposure(target: Coordinate, exposure: Coordinate, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'toggleTargetExposure($target, $exposure, $groupHistory)' is not supported!" }
    }

    fun togglePathExposure(path: Path, exposure: Coordinate, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'togglePathExposure($path, $exposure, $groupHistory)' is not supported!" }
    }

    fun togglePathSelect(point: Coordinate, direction: Direction, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'togglePathSelect($point, $direction, $groupHistory)' is not supported!" }
    }

    fun setStartPoint(point: Coordinate, orientation: Direction, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setStartPoint($point, $orientation, $groupHistory)' is not supported!" }
    }

    fun deleteStartPoint(groupHistory: Boolean) {
        logger.warn { "Plotter action 'deleteStartPoint($groupHistory)' is not supported!" }
    }

    fun setBluePoint(point: Coordinate, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setBluePoint($point, $groupHistory)' is not supported!" }
    }

    fun togglePathHiddenState(path: Path, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'togglePathHiddenState($path, $groupHistory)' is not supported!" }
    }

    fun setPathWeight(path: Path, weight: Int, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setPathWeight($path, $weight, $groupHistory)' is not supported!" }
    }
    
    fun createComment(value: String, position: Point, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'createComment($value, $position, $groupHistory)' is not supported!" }
    }

    fun setCommentValue(comment: Comment, value: String, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setCommentValue($comment, $value, $groupHistory)' is not supported!" }
    }

    fun setCommentPosition(comment: Comment, position: Point, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setCommentPosition($comment, $position, $groupHistory)' is not supported!" }
    }

    fun deleteComment(comment: Comment, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'deleteComment($comment, $groupHistory)' is not supported!" }
    }


    fun undo() {
        logger.warn { "Plotter action 'undo()' is not supported!" }
    }

    fun redo() {
        logger.warn { "Plotter action 'redo()' is not supported!" }
    }
}
