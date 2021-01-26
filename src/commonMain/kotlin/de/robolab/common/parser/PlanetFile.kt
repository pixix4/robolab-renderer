package de.robolab.common.parser

import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.utils.History
import de.robolab.common.parser.lines.*
import de.robolab.common.planet.*
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.westermann.kobserve.property.mapBinding

class PlanetFile(lines: List<String>) : IEditCallback {

    constructor(content: String) : this(content.split('\n'))

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

    private fun parseFileContent(fileContent: List<String>) = fileContent.map { parseLine(it) }

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
        var newLines = lines + PathLine.create(
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
            newLines = newLines + SplineLine.create(controlPoints)
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

        val index = newLines.indexOfFirst { it is SplineLine && it.associatedPath?.equalPath(path) == true }
        if (controlPoints.isEmpty()) {
            if (index >= 0) {
                newLines.removeAt(index)
            }
        } else {
            val newLine = SplineLine.create(controlPoints)

            if (index < 0) {
                val pathIndex = newLines.indexOfFirst {
                    it is PathLine && it.data.equalPath(path) || it is StartPointLine && it.data.path.equalPath(
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
            lines + TargetLine.create(targetPoint)
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

        val index = newLines.indexOfFirst { it is PathLine && it.data.equalPath(path) }
        if (index < 0) {
            return
        }

        val p = newLines[index].data as? Path ?: return

        newLines[index] = PathLine.create(
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
            lines + PathSelectLine.create(pathSelect)
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

        val index = lines.indexOfFirst { it is StartPointLine }

        val newLines = if (index < 0) {
            listOf(StartPointLine.create(startPoint)) + lines
        } else {
            val list = lines.toMutableList()
            val old = planet.startPoint
            if (old != null) {
                list.retainAll { !it.isAssociatedTo(old) }
            }
            list.add(index, StartPointLine.create(startPoint))
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
        val index = lines.indexOfFirst { it is BluePointLine }

        val newLines = lines.toMutableList()
        if (index < 0) {
            newLines.add(BluePointLine.create(point))
        } else {
            newLines[index] = BluePointLine.create(point)
        }

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun togglePathHiddenState(path: Path, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is HiddenLine && it.associatedPath?.equalPath(path) == true }
        if (path.hidden) {
            if (index >= 0) {
                newLines.removeAt(index)
            }
        } else {
            val newLine = HiddenLine.create()

            if (index < 0) {
                val pathIndex = newLines.indexOfFirst { it is PathLine && it.data.equalPath(path) }
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

        val index = newLines.indexOfFirst { it is PathLine && it.data.equalPath(path) }
        if (index < 0) {
            return
        }

        val p = newLines[index].data as? Path ?: return

        newLines[index] = PathLine.create(p.copy(weight = weight))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun createComment(value: List<String>, position: Point, groupHistory: Boolean) {
        val comment = Comment(position, Comment.Alignment.CENTER, value)

        val newLines = lines + CommentLine.createAll(comment)

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentValue(comment: Comment, value: List<String>, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is CommentLine && it.isAssociatedTo(comment) }
        if (index < 0) {
            return
        }

        newLines.removeAll {
            it.isAssociatedTo(comment)
        }
        newLines.addAll(index, CommentLine.createAll(comment.copy(lines = value)))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentPosition(comment: Comment, position: Point, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is CommentLine && it.isAssociatedTo(comment) }
        if (index < 0) {
            return
        }

        newLines.removeAll {
            it.isAssociatedTo(comment)
        }
        newLines.addAll(index, CommentLine.createAll(comment.copy(point = position)))

        if (groupHistory) {
            history.replace(newLines)
        } else {
            lines = newLines
        }
    }

    override fun setCommentAlignment(comment: Comment, alignment: Comment.Alignment, groupHistory: Boolean) {
        val newLines = lines.toMutableList()

        val index = newLines.indexOfFirst { it is CommentLine && it.isAssociatedTo(comment) }
        if (index < 0) {
            return
        }

        newLines.removeAll {
            it.isAssociatedTo(comment)
        }
        newLines.addAll(index, CommentLine.createAll(comment.copy(alignment = alignment)))

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
                is StartPointLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = StartPointLine.create(obj)
                }
                is BluePointLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = BluePointLine.create(obj)
                }
                is PathLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = PathLine.create(obj)
                }
                is SplineLine -> {
                    val obj = line.data.map { it + delta.toPoint() }
                    newLines[i] = SplineLine.create(obj)
                }
                is TargetLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = TargetLine.create(obj)
                }
                is PathSelectLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = PathSelectLine.create(obj)
                }
                is CommentLine -> {
                    val obj = line.data.translate(delta)
                    newLines[i] = CommentLine.create(obj)
                }
                is GroupingLine -> {
                    newLines[i] = GroupingLine.create(
                        line.data.second.map { it.translate(delta) }.toSet(),
                        line.data.first
                    )
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
                is StartPointLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = StartPointLine.create(obj)
                }
                is BluePointLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = BluePointLine.create(obj)
                }
                is PathLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = PathLine.create(obj)
                }
                is SplineLine -> {
                    val obj = line.data.map { it.rotate(direction.angle, origin.toPoint()) }
                    newLines[i] = SplineLine.create(obj)
                }
                is TargetLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = TargetLine.create(obj)
                }
                is PathSelectLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = PathSelectLine.create(obj)
                }
                is CommentLine -> {
                    val obj = line.data.rotate(direction, origin)
                    newLines[i] = CommentLine.create(obj)
                }
                is GroupingLine -> {
                    newLines[i] = GroupingLine.create(
                        line.data.second.map { it.rotate(direction, origin) }.toSet(),
                        line.data.first
                    )
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
                is PathLine -> {
                    val obj = line.data.scaleWeights(factor, offset)
                    newLines[i] = PathLine.create(obj)
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
        val index = lines.indexOfFirst { it is NameLine }

        val newLines = lines.toMutableList()
        if (index < 0) {
            newLines.add(NameLine.create(name))
        } else {
            newLines[index] = NameLine.create(name)
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

    fun format(explicit: Boolean = false) {
        history.push(parseFileContent(createFromPlanet(planet, explicit, this).content))
    }

    companion object {
        fun getName(text: String): String? {
            val lines = text.split('\n')
            return getName(lines)
        }

        fun getName(lines: List<String>): String? {
            for (line in lines) {
                val l = parseLine(line)

                if (l is NameLine) {
                    return l.data
                }
            }

            return null
        }

        fun createFromPlanet(
            planet: Planet,
            includeEmptySplines: Boolean = false,
            includeComments: PlanetFile? = null,
            includeTests: Boolean = true,
        ): PlanetFile {
            val lines = mutableListOf<FileLine<*>>()

            if (planet.name.isNotBlank()) {
                lines += NameLine.create(planet.name)
            }
            lines += VersionLine.create(planet.version)

            lines += BlankLine.create()
            for ((key, value) in planet.tagMap) {
                lines += TagLine.create(Tag(key, value))
            }

            lines += BlankLine.create()
            if (planet.startPoint != null) {
                lines += StartPointLine.create(planet.startPoint)
                if (includeEmptySplines) {
                    val points = PathAnimatable
                        .getControlPointsFromPath(planet.version, planet.startPoint.path)
                        .drop(1)
                        .dropLast(if (planet.startPoint.path.isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 0 else 1)
                    if (points.isNotEmpty()) {
                        lines += SplineLine.create(points)
                    }
                } else {
                    if (planet.startPoint.controlPoints.isNotEmpty()) {
                        val points = PathAnimatable
                            .getControlPointsFromPath(
                                planet.version,
                                planet.startPoint.path.copy(controlPoints = emptyList())
                            )
                            .drop(1)
                            .dropLast(if (planet.startPoint.path.isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 0 else 1)
                        if (planet.startPoint.controlPoints != points) {
                            lines += SplineLine.create(planet.startPoint.controlPoints)
                        }
                    }
                }
            }
            if (planet.bluePoint != null) {
                lines += BluePointLine.create(planet.bluePoint)
            }

            lines += BlankLine.create()
            for (path in planet.pathList) {
                lines += PathLine.create(path)
                if (includeEmptySplines) {
                    val points = PathAnimatable
                        .getControlPointsFromPath(planet.version, path)
                        .drop(1)
                        .dropLast(if (path.isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 0 else 1)
                    if (points.isNotEmpty()) {
                        lines += SplineLine.create(points)
                    }
                } else {
                    if (path.controlPoints.isNotEmpty()) {
                        val points = PathAnimatable
                            .getControlPointsFromPath(planet.version, path.copy(controlPoints = emptyList()))
                            .drop(1)
                            .dropLast(if (path.isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 0 else 1)
                        if (path.controlPoints != points) {
                            lines += SplineLine.create(path.controlPoints)
                        }
                    }
                }
                if (path.hidden) {
                    lines += HiddenLine.create()
                }
            }

            lines += BlankLine.create()
            for (pathSelect in planet.pathSelectList) {
                lines += PathSelectLine.create(pathSelect)
            }

            lines += BlankLine.create()
            for (target in planet.targetList) {
                lines += TargetLine.create(target)
            }

            lines += BlankLine.create()
            val grouping = if (includeEmptySplines) {
                planet.senderGrouping
            } else {
                val keysToRemove = planet.getDefaultSenderGroupings().filter { (set, char) ->
                    planet.senderGrouping[set] == char
                }.keys

                planet.senderGrouping - keysToRemove
            }
            for ((set, char) in grouping) {
                lines += GroupingLine.create(set, char)
            }

            lines += BlankLine.create()
            for (comment in planet.commentList) {
                lines += CommentLine.createAll(comment)
            }

            val filteredLines = lines.fold(emptyList<FileLine<*>>()) { acc, line ->
                if (line is BlankLine && acc.lastOrNull() is BlankLine) acc else acc + line
            }

            val withComments = if (includeComments == null) filteredLines else {
                filteredLines.fold(emptyList()) { acc, line ->
                    val h = includeComments.lines.find { it == line }

                    val extra = if (h != null && h.blockMode is FileLine.BlockMode.Head) {
                        includeComments.lines.filter {
                            val blockMode = it.blockMode
                            blockMode is FileLine.BlockMode.Append &&
                                    blockMode.blockHead == h &&
                                    it !is SplineLine &&
                                    it !is HiddenLine &&
                                    it !is CommentSubLine
                        }
                    } else emptyList()

                    acc + line + extra
                }
            }

            val withTests = if (!includeTests || planet.testSuite == TestSuite.EMPTY) withComments else {
                val tests = mutableListOf<FileLine<*>>()
                tests += BlankLine.create()

                if (planet.testSuite.goal != null) {
                    tests += TestGoalLine.create(planet.testSuite.goal)
                }
                for (t in planet.testSuite.taskList) {
                    tests += TestTaskLine.create(t)
                }
                for (t in planet.testSuite.triggerList) {
                    tests += TestTriggerLine.create(t)
                }
                for (t in planet.testSuite.modifierList) {
                    tests += TestModifierLine.create(t)
                }

                withComments + tests
            }

            return PlanetFile(withTests.map { it.line })
        }
    }
}
