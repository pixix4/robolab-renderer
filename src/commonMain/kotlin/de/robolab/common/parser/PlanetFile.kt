package de.robolab.common.parser

import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.utils.History
import de.robolab.common.planet.*
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.westermann.kobserve.property.mapBinding

class PlanetFile(fileContent: String) : IEditCallback {

    private val logger = Logger(this)
    val history = History(parseFileContent(fileContent))
    private var lines by history

    val planetProperty = history.mapBinding { lines ->
        val buildAccumulator = FileLine.BuildAccumulator()
        for (line in lines) {
            try {
                line.buildPlanet(buildAccumulator)
            } catch (e: Exception) {
                logger.error { "Error while buildPlanet, ${line::class.simpleName}: '${line.line}'" }
            }
        }
        buildAccumulator.planet
    }
    val planet by planetProperty

    private fun parseFileContent(fileContent: String) = fileContent.split('\n').map { parseLine(it) }

    var content: String
        get() = lines.joinToString("\n") { it.line }
        set(value) {
            lines = parseFileContent(value)
        }

    fun replaceContent(fileContent: String) {
        history.replace(parseFileContent(fileContent))
    }

    fun resetContent(fileContent: String) {
        history.clear(parseFileContent(fileContent))
    }

    override fun createPath(
        startPoint: Coordinate,
        startDirection: Direction,
        endPoint: Coordinate,
        endDirection: Direction,
        controlPoints: List<Point>,
        groupHistory: Boolean
    ) {
        var newLines = lines + FileLine.PathLine.create(
            Path(
                startPoint, startDirection,
                endPoint, endDirection,
                if (startPoint == endPoint && startDirection == endDirection) -1 else 1,
                emptySet(),
                controlPoints,
                false
            )
        )

        if (controlPoints.isNotEmpty()) {
            newLines = newLines + FileLine.SplineLine.create(controlPoints)
        }

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

        val index = newLines.indexOfFirst { it is FileLine.SplineLine && it.associatedPath?.equalPath(path) == true }
        if (controlPoints.isEmpty()) {
            if (index >= 0) {
                newLines.removeAt(index)
            }
        } else {
            val newLine = FileLine.SplineLine.create(controlPoints)

            if (index < 0) {
                val pathIndex = newLines.indexOfFirst {
                    it is FileLine.PathLine && it.data.equalPath(path) || it is FileLine.StartPointLine && it.data.path.equalPath(
                        path
                    )
                }
                newLines.add(pathIndex + 1, newLine)
            } else {
                newLines[index] = newLine
            }
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

        newLines[index] = FileLine.PathLine.create(
            if (exposure in p.exposure) {
                p.copy(exposure = p.exposure - exposure)
            } else {
                p.copy(exposure = p.exposure + exposure)
            }
        )

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

    override fun setStartPoint(point: Coordinate, orientation: Direction, groupHistory: Boolean) {
        val startPoint = StartPoint(point, orientation, emptyList())

        val index = lines.indexOfFirst { it is FileLine.StartPointLine }

        val newLines = if (index < 0) {
            listOf(FileLine.StartPointLine.create(startPoint)) + lines
        } else {
            val list = lines.toMutableList()
            val old = planet.startPoint
            if (old != null) {
                list.retainAll { !it.isAssociatedTo(old) }
            }
            list.add(index, FileLine.StartPointLine.create(startPoint))
            list
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun deleteStartPoint(groupHistory: Boolean) {
        val newLines = lines.toMutableList()
        val startPoint = planet.startPoint ?: return
        newLines.retainAll { !it.isAssociatedTo(startPoint) }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setBluePoint(point: Coordinate, groupHistory: Boolean) {
        val index = lines.indexOfFirst { it is FileLine.BluePointLine }

        val newLines = lines.toMutableList()
        if (index < 0) {
            newLines.add(FileLine.BluePointLine.create(point))
        } else {
            newLines[index] = FileLine.BluePointLine.create(point)
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun togglePathHiddenState(path: Path, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.HiddenLine && it.associatedPath?.equalPath(path) == true }
        if (path.hidden) {
            if (index >= 0) {
                newLines.removeAt(index)
            }
        } else {
            val newLine = FileLine.HiddenLine.create()

            if (index < 0) {
                val pathIndex = newLines.indexOfFirst { it is FileLine.PathLine && it.data.equalPath(path) }
                newLines.add(pathIndex + 1, newLine)
            } else {
                newLines[index] = newLine
            }
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setPathWeight(path: Path, weight: Int, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.PathLine && it.data.equalPath(path) }
        if (index < 0) {
            return
        }

        val p = newLines[index].data as? Path ?: return

        newLines[index] = FileLine.PathLine.create(p.copy(weight = weight))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun createComment(value: String, position: Point, groupHistory: Boolean) {
        val comment = Comment(position, value)

        val newLines = lines + FileLine.CommentLine.create(comment)

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentValue(comment: Comment, value: String, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.CommentLine && it.data == comment }
        if (index < 0) {
            return
        }

        val p = newLines[index].data as? Comment ?: return

        newLines[index] = FileLine.CommentLine.create(p.copy(message = value))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentPosition(comment: Comment, position: Point, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.CommentLine && it.data == comment }
        if (index < 0) {
            return
        }

        val p = newLines[index].data as? Comment ?: return

        newLines[index] = FileLine.CommentLine.create(p.copy(point = position))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun deleteComment(comment: Comment, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.CommentLine && it.data == comment }
        if (index < 0) {
            return
        }

        newLines.removeAt(index)

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
