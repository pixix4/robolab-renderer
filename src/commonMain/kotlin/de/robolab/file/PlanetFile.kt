package de.robolab.file

import de.robolab.model.*
import de.robolab.renderer.History
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.edit.IEditCallback
import de.westermann.kobserve.property.mapBinding

class PlanetFile(fileContent: String) : IEditCallback {

    val history = History(parseFileContent(fileContent))
    var lines by history.valueProperty

    val planet = history.valueProperty.mapBinding { lines ->
        val buildAccumulator = FileLine.BuildAccumulator()
        for (line in lines) {
            try {
                line.buildPlanet(buildAccumulator)
            } catch (e: Exception) {
                println(e)
            }
        }
        buildAccumulator.planet
    }

    private fun parseFileContent(fileContent: String) = fileContent.split('\n').map { parseLine(it) }

    fun setContent(fileContent: String) {
        lines = parseFileContent(fileContent)
    }

    override fun createPath(startPoint: Coordinate, startDirection: Direction, endPoint: Coordinate, endDirection: Direction, groupHistory: Boolean) {
        val newLines = lines + FileLine.PathLine.create(Path(
                startPoint, startDirection,
                endPoint, endDirection,
                1,
                emptySet(),
                emptyList(),
                false
        ))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun deletePath(path: Path, groupHistory: Boolean) {
        val newLines = lines - lines.filter {
            it.isAssociatedTo(path)
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun updatePathControlPoints(path: Path, controlPoints: List<Point>, groupHistory: Boolean) {
        val newLines = lines.toMutableList()
        val newLine = FileLine.SplineLine.create(controlPoints)

        val index = newLines.indexOfFirst { it is FileLine.SplineLine && it.associatedPath?.equalPath(path) == true }
        if (index < 0) {
            val pathIndex = newLines.indexOfFirst { it is FileLine.PathLine && it.data.equalPath(path) }
            newLines.add(pathIndex + 1, newLine)
        } else {
            newLines[index] = newLine
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun toggleTargetExposure(target: Coordinate, exposure: Coordinate, groupHistory: Boolean) {
        val targetPoint = TargetPoint(target, exposure)
        val senderLines = lines.filter { it.isAssociatedTo(targetPoint) }

        val newLines = if (senderLines.isEmpty()) {
            lines + FileLine.TargetLine.create(targetPoint)
        } else {
            lines - senderLines
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun togglePathExposure(path: Path, exposure: Coordinate, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.PathLine && it.data.equalPath(path) }
        if (index < 0) {
            return
        }

        val p = newLines[index].data as? Path ?: return

        newLines[index] = FileLine.PathLine.create(if (exposure in p.exposure) {
            p.copy(exposure = p.exposure - exposure)
        } else {
            p.copy(exposure = p.exposure + exposure)
        })

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun togglePathSelect(point: Coordinate, direction: Direction, groupHistory: Boolean) {
        val pathSelect = PathSelect(point, direction)
        val pathSelectLine = lines.filter { it.isAssociatedTo(pathSelect) }

        val newLines = if (pathSelectLine.isEmpty()) {
            lines + FileLine.PathSelectLine.create(pathSelect)
        } else {
            lines - pathSelectLine
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun undo() {
        history.undo()
    }

    override fun redo() {
        history.redo()
    }
}
