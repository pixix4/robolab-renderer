package de.robolab.common.parser

import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.utils.History
import de.robolab.common.planet.*
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.westermann.kobserve.property.mapBinding

class PlanetFile(lines: List<String>) : IEditCallback {

    constructor(content: String): this(content.split('\n'))

    private val logger = Logger(this)
    val history = History(parseFileContent(lines))
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
        buildAccumulator.planet.generateMissingSenderGroupings()
    }
    val planet by planetProperty

    private fun parseFileContent(lines: List<String>) = lines.map { parseLine(it) }

    var contentString: String
        get() = lines.joinToString("\n") { it.line }
        set(value) {
            lines = parseFileContent(value.split('\n'))
        }

    var content: List<String>
        get() = lines.map { it.line }
        set(value) {
            lines = parseFileContent(value)
        }

    fun replaceContent(lines: List<String>) {
        history.replace(parseFileContent(lines))
    }

    fun resetContent(lines: List<String>) {
        history.clear(parseFileContent(lines))
    }

    fun replaceContent(content: String) {
        history.replace(parseFileContent(content.split('\n')))
    }

    fun resetContent(content: String) {
        history.clear(parseFileContent(content.split('\n')))
    }

    fun valueToLineNumber(value: IPlanetValue): Int? {
        val index = lines.indexOfFirst { it.isAssociatedTo(value) }
        if (index < 0) return null
        return index
    }

    fun lineNumberToValue(lineNumber: Int): IPlanetValue? {
        return lines.getOrNull(lineNumber)?.data as? IPlanetValue
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
                hidden = false,
                showDirectionArrow = false
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

    override fun createComment(value: List<String>, position: Point, groupHistory: Boolean) {
        val comment = Comment(position, Comment.Alignment.CENTER, value)

        val newLines = lines + FileLine.CommentLine.createAll(comment)

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentValue(comment: Comment, value: List<String>, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.CommentLine && it.isAssociatedTo(comment) }
        if (index < 0) {
            return
        }

        newLines.removeAll {
            it.isAssociatedTo(comment)
        }
        newLines.addAll(index, FileLine.CommentLine.createAll(comment.copy(lines = value)))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentPosition(comment: Comment, position: Point, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.CommentLine && it.isAssociatedTo(comment) }
        if (index < 0) {
            return
        }

        newLines.removeAll {
            it.isAssociatedTo(comment)
        }
        newLines.addAll(index, FileLine.CommentLine.createAll(comment.copy(point = position)))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentAlignment(comment: Comment, alignment: Comment.Alignment, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is FileLine.CommentLine && it.isAssociatedTo(comment) }
        if (index < 0) {
            return
        }

        newLines.removeAll {
            it.isAssociatedTo(comment)
        }
        newLines.addAll(index, FileLine.CommentLine.createAll(comment.copy(alignment = alignment)))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun deleteComment(comment: Comment, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        newLines.removeAll {
            it.isAssociatedTo(comment)
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

    override fun translate(delta: Coordinate, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        for (i in 0 until newLines.size) {
            when (val line = newLines[i]) {
                is FileLine.StartPointLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = FileLine.StartPointLine.create(obj)
                }
                is FileLine.BluePointLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = FileLine.BluePointLine.create(obj)
                }
                is FileLine.PathLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = FileLine.PathLine.create(obj)
                }
                is FileLine.SplineLine -> {
                    val obj = line.data.map { it + delta.toPoint() }
                    newLines[i] = FileLine.SplineLine.create(obj)
                }
                is FileLine.TargetLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = FileLine.TargetLine.create(obj)
                }
                is FileLine.PathSelectLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = FileLine.PathSelectLine.create(obj)
                }
                is FileLine.CommentLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = FileLine.CommentLine.create(obj)
                }
            }
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun rotate(direction: Planet.RotateDirection, origin: Coordinate, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        for (i in 0 until newLines.size) {
            when (val line = newLines[i]) {
                is FileLine.StartPointLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = FileLine.StartPointLine.create(obj)
                }
                is FileLine.BluePointLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = FileLine.BluePointLine.create(obj)
                }
                is FileLine.PathLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = FileLine.PathLine.create(obj)
                }
                is FileLine.SplineLine -> {
                    val obj = line.data.map { it.rotate(direction.angle, origin.toPoint()) }
                    newLines[i] = FileLine.SplineLine.create(obj)
                }
                is FileLine.TargetLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = FileLine.TargetLine.create(obj)
                }
                is FileLine.PathSelectLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = FileLine.PathSelectLine.create(obj)
                }
                is FileLine.CommentLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = FileLine.CommentLine.create(obj)
                }
            }
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun scaleWeights(factor: Double, offset: Int, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        for (i in 0 until newLines.size) {
            when (val line = newLines[i]) {
                is FileLine.PathLine -> {
                    val obj = line.data.scaleWeights(factor, offset)
                    newLines[i] = FileLine.PathLine.create(obj)
                }
            }
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setName(name: String, groupHistory: Boolean) {
        val index = lines.indexOfFirst { it is FileLine.NameLine }

        val newLines = lines.toMutableList()
        if (index < 0) {
            newLines.add(FileLine.NameLine.create(name))
        } else {
            newLines[index] = FileLine.NameLine.create(name)
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    fun extendedContentString(): String {
        return createFromPlanet(planet, true).contentString
    }

    companion object {
        fun getName(text: String): String? {
            val lines = text.split('\n')
            return getName(lines)
        }
        fun getName(lines: List<String>): String? {
            for (line in lines) {
                val l = parseLine(line)

                if (l is FileLine.NameLine) {
                    return l.data
                }
            }

            return null
        }

        fun createFromPlanet(planet: Planet, includeEmptySplines: Boolean = false): PlanetFile {
            val lines = mutableListOf<FileLine<*>>()

            if (planet.name.isNotBlank()) {
                lines += FileLine.NameLine.create(planet.name)
            }
            lines += FileLine.VersionLine.create(planet.version)

            lines += FileLine.BlankLine.create()
            for ((key, value) in planet.tagMap) {
                lines += FileLine.TagLine.create(Tag(key, value))
            }

            lines += FileLine.BlankLine.create()
            if (planet.startPoint != null) {
                lines += FileLine.StartPointLine.create(planet.startPoint)
                if (includeEmptySplines) {
                    val points = PathAnimatable.getControlPointsFromPath(planet.version, planet.startPoint.path).drop(1).dropLast(1)
                    if (points.isNotEmpty()) {
                        lines += FileLine.SplineLine.create(points)
                    }
                } else {
                    if (planet.startPoint.controlPoints.isNotEmpty()) {
                        lines += FileLine.SplineLine.create(planet.startPoint.controlPoints)
                    }
                }
            }
            if (planet.bluePoint != null) {
                lines += FileLine.BluePointLine.create(planet.bluePoint)
            }

            lines += FileLine.BlankLine.create()
            for (path in planet.pathList) {
                lines += FileLine.PathLine.create(path)
                if (includeEmptySplines) {
                    val points = PathAnimatable.getControlPointsFromPath(planet.version, path).drop(1).dropLast(1)
                    if (points.isNotEmpty()) {
                        lines += FileLine.SplineLine.create(points)
                    }
                } else {
                    if (path.controlPoints.isNotEmpty()) {
                        lines += FileLine.SplineLine.create(path.controlPoints)
                    }
                }
                if (path.hidden) {
                    lines += FileLine.HiddenLine.create()
                }
            }

            lines += FileLine.BlankLine.create()
            for (pathSelect in planet.pathSelectList) {
                lines += FileLine.PathSelectLine.create(pathSelect)
            }

            lines += FileLine.BlankLine.create()
            for (target in planet.targetList) {
                lines += FileLine.TargetLine.create(target)
            }

            lines += FileLine.BlankLine.create()
            for ((set, char) in planet.senderGrouping) {
                lines += FileLine.GroupingLine.create(set, char)
            }

            lines += FileLine.BlankLine.create()
            for (comment in planet.commentList) {
                lines += FileLine.CommentLine.createAll(comment)
            }

            val stringLines = lines.fold(emptyList<FileLine<*>>()) { acc, line ->
                if (line is FileLine.BlankLine && acc.lastOrNull() is FileLine.BlankLine) acc else acc + line
            }.map { it.line }
            return PlanetFile(stringLines)
        }
    }
}
