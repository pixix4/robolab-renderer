package de.robolab.client.renderer.drawable.edit

import de.robolab.common.planet.*
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Vector

private val logger = Logger("IEditCallback")

interface IEditCallback {

    fun createPath(
        source: PlanetPoint,
        sourceDirection: PlanetDirection,
        target: PlanetPoint,
        targetDirection: PlanetDirection,
        spline: PlanetSpline?,
        groupHistory: Boolean = false,
    ) {
        logger.warn { "Plotter action 'drawPath($source, $sourceDirection, $target, $targetDirection, $spline, $groupHistory)' is not supported!" }
    }

    fun updatePathSpline(path: PlanetPath, spline: PlanetSpline?, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'updateControlPoints($path, $spline, $groupHistory)' is not supported!" }
    }

    fun deletePath(path: PlanetPath, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'deletePath($path, $groupHistory)' is not supported!" }
    }

    fun toggleTargetExposure(target: PlanetPoint, exposure: PlanetPoint, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'toggleTargetExposure($target, $exposure, $groupHistory)' is not supported!" }
    }

    fun togglePathExposure(path: PlanetPath, exposure: PlanetPathExposure, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'togglePathExposure($path, $exposure, $groupHistory)' is not supported!" }
    }

    fun togglePathSelect(point: PlanetPoint, direction: PlanetDirection, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'togglePathSelect($point, $direction, $groupHistory)' is not supported!" }
    }

    fun setStartPoint(point: PlanetPoint, orientation: PlanetDirection, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setStartPoint($point, $orientation, $groupHistory)' is not supported!" }
    }

    fun deleteStartPoint(groupHistory: Boolean) {
        logger.warn { "Plotter action 'deleteStartPoint($groupHistory)' is not supported!" }
    }

    fun setBluePoint(point: PlanetPoint, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setBluePoint($point, $groupHistory)' is not supported!" }
    }

    fun togglePathHiddenState(path: PlanetPath, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'togglePathHiddenState($path, $groupHistory)' is not supported!" }
    }

    fun setPathWeight(path: PlanetPath, weight: Long, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setPathWeight($path, $weight, $groupHistory)' is not supported!" }
    }

    fun createComment(value: List<String>, position: Vector, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'createComment($value, $position, $groupHistory)' is not supported!" }
    }

    fun setCommentValue(comment: PlanetComment, value: List<String>, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setCommentValue($comment, $value, $groupHistory)' is not supported!" }
    }

    fun setCommentPosition(comment: PlanetComment, position: Vector, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setCommentPosition($comment, $position, $groupHistory)' is not supported!" }
    }

    fun setCommentAlignment(comment: PlanetComment, alignment: PlanetCommentAlignment, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setCommentAlignment($comment, $alignment, $groupHistory)' is not supported!" }
    }

    fun deleteComment(comment: PlanetComment, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'deleteComment($comment, $groupHistory)' is not supported!" }
    }


    fun translate(delta: PlanetPoint, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'translate($delta, $groupHistory)' is not supported!" }
    }

    fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'rotate($direction, $origin, $groupHistory)' is not supported!" }
    }

    fun scaleWeights(factor: Double, offset: Long, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'scaleWeights($factor, $offset, $groupHistory)' is not supported!" }
    }

    fun setName(name: String, groupHistory: Boolean = false) {
        logger.warn { "Plotter action 'setName($name, $groupHistory)' is not supported!" }
    }


    fun undo() {
        logger.warn { "Plotter action 'undo()' is not supported!" }
    }

    fun redo() {
        logger.warn { "Plotter action 'redo()' is not supported!" }
    }
}
