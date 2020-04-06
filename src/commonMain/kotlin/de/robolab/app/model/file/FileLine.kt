package de.robolab.app.model.file

import de.robolab.model.*
import de.robolab.renderer.data.Point
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.round

interface FileLine<T> {
    val line: String
    val data: T

    fun buildPlanet(builder: BuildAccumulator)
    fun isAssociatedTo(obj: Any): Boolean {
        return (blockMode as? BlockMode.Append)?.blockHead?.isAssociatedTo(obj) == true
    }

    val blockMode: BlockMode

    sealed class BlockMode(val previousBlockHead: FileLine<*>?) {
        class Head(
                previousBlockHead: FileLine<*>?
        ) : BlockMode(previousBlockHead)

        class Append(
                val blockHead: FileLine<*>,
                previousBlockHead: FileLine<*>?
        ) : BlockMode(previousBlockHead) {
            constructor(blockHead: FileLine<*>) : this(blockHead, blockHead.blockMode.previousBlockHead)
        }

        class Skip(
                previousBlockHead: FileLine<*>?
        ) : BlockMode(previousBlockHead)
    }

    class BuildAccumulator(
            var planet: Planet = Planet.EMPTY,
            var previousBlockHead: FileLine<*>? = null
    )

    interface Parser {
        val name: String
        fun testLine(line: String): Boolean
        fun createInstance(line: String): FileLine<*>
    }

    class BlankLine : FileLine<Unit> {
        override val line = ""
        override val data = Unit

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Skip(builder.previousBlockHead)
        }

        companion object : Parser {
            override val name = "Blank line parser"
            override fun testLine(line: String): Boolean {
                return line.isBlank()
            }

            override fun createInstance(line: String): FileLine<*> {
                return BlankLine()
            }

            fun create() = createInstance("")
        }
    }

    class UnknownLine(override val line: String) : FileLine<Unit> {

        override val data = Unit

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Append(builder.previousBlockHead!!)
        }

    }

    class PathLine(override val line: String) : FileLine<Path> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            Path(
                    parseCoordinate(match.groupValues[1]),
                    parseDirection(match.groupValues[2])!!,
                    parseCoordinate(match.groupValues[3]),
                    parseDirection(match.groupValues[4])!!,
                    match.groupValues[5].toInt(),
                    match.groupValues.getOrNull(6)?.split(" ")?.filter { it.isNotBlank() }?.map { parseCoordinate(it) }?.toSet()
                            ?: emptySet(),
                    emptyList(),
                    false
            )
        }

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                    pathList = builder.planet.pathList + data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj !is Path) return false

            return obj.equalPath(data)
        }

        companion object : Parser {
            override val name = "Path line parser"
            private val REGEX =
                    """^(-?\d+,-?\d+),([NESW]) (-?\d+,-?\d+),([NESW]) (-?\d+)((?: -?\d+,-?\d+)*)( blocked)?\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return PathLine(line)
            }

            fun serialize(path: Path) = buildString {
                append(serializeCoordinate(path.source))
                append(',')
                append(serializeDirection(path.sourceDirection))
                append(' ')
                append(serializeCoordinate(path.target))
                append(',')
                append(serializeDirection(path.targetDirection))
                append(' ')
                append(path.weight ?: 1)
                for (exposure in path.exposure) {
                    append(' ')
                    append(serializeCoordinate(exposure))
                }
                if (path.weight != null && path.weight < 0) {
                    append(" blocked")
                }
            }

            fun create(path: Path) = createInstance(serialize(path))
        }
    }

    class StartPointLine(override val line: String) : FileLine<StartPoint> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            StartPoint(
                    parseCoordinate(match.groupValues[1]),
                    parseDirection(match.groupValues[3]) ?: Direction.NORTH,
                    emptyList()
            )
        }

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                    startPoint = data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj is Path) {
                if (obj.equalPath(data.path)) {
                    return true
                }
            }
            if (obj !is StartPoint) return false

            return obj == data
        }

        companion object : Parser {
            override val name = "Start point line parser"
            private val REGEX =
                    """^start (-?\d+,-?\d+)(,([NESW]))?\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return StartPointLine(line)
            }

            fun serialize(startPoint: StartPoint) = buildString {
                append("start ")
                append(serializeCoordinate(startPoint.point))
                if (startPoint.orientation != Direction.NORTH) {
                    append(',')
                    append(serializeDirection(startPoint.orientation))
                }
            }

            fun create(startPoint: StartPoint) = createInstance(serialize(startPoint))
        }
    }

    class BluePointLine(override val line: String) : FileLine<Coordinate> {

        override val data = parseCoordinate(REGEX.matchEntire(line.trim())!!.groupValues[1])

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                    bluePoint = data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj !is Coordinate) return false

            return obj == data
        }

        companion object : Parser {
            override val name = "Blue point line parser"
            private val REGEX =
                    """^blue (-?\d+,-?\d+)\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return BluePointLine(line)
            }

            fun serialize(bluePoint: Coordinate) = buildString {
                append("blue ")
                append(serializeCoordinate(bluePoint))
            }

            fun create(bluePoint: Coordinate) = createInstance(serialize(bluePoint))
        }
    }

    class PathSelectLine(override val line: String) : FileLine<PathSelect> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            PathSelect(
                    parseCoordinate(match.groupValues[2]),
                    parseDirection(match.groupValues[1])!!
            )
        }

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                    pathSelectList = builder.planet.pathSelectList + data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj !is PathSelect) return false

            return obj == data
        }

        companion object : Parser {
            override val name = "Path select line parser"
            private val REGEX =
                    """^direction ([NSWE]) (-?\d+,-?\d+)\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return PathSelectLine(line)
            }

            fun serialize(pathSelect: PathSelect) = buildString {
                append("direction ")
                append(serializeDirection(pathSelect.direction))
                append(' ')
                append(serializeCoordinate(pathSelect.point))
            }

            fun create(pathSelect: PathSelect) = createInstance(serialize(pathSelect))
        }
    }

    class TargetLine(override val line: String) : FileLine<TargetPoint> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            TargetPoint(
                    parseCoordinate(match.groupValues[1]),
                    parseCoordinate(match.groupValues[2])
            )
        }

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                    targetList = builder.planet.targetList + data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj !is TargetPoint) return false

            return obj == data
        }

        companion object : Parser {
            override val name = "Target line parser"
            private val REGEX =
                    """^target (-?\d+,-?\d+) (-?\d+,-?\d+)\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return TargetLine(line)
            }

            fun serialize(target: TargetPoint) = buildString {
                append("target ")
                append(serializeCoordinate(target.target))
                append(' ')
                append(serializeCoordinate(target.exposure))
            }

            fun create(target: TargetPoint) = createInstance(serialize(target))
        }
    }

    class NameLine(override val line: String) : FileLine<String> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            match.groupValues[2]
        }

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                    name = data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj !is String) return false

            return obj == data
        }

        companion object : Parser {
            override val name = "Name line parser"
            private val REGEX =
                    """^#\s*(NAME|name)\s?(?::\s*(\w[^\n]*?))?\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return NameLine(line)
            }

            fun serialize(name: String) = "# name: $name"
            fun create(name: String) = createInstance(serialize(name))
        }
    }

    class SplineLine(override val line: String) : FileLine<List<Point>> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            match.groupValues[3].split('|').mapNotNull { p ->
                val h = p.split(',').map { it.trim().toDouble() }
                if (h.size < 2) null else Point(h[0], h[1])
            }
        }

        var associatedPath: Path? = null

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            val previousBlockHead = builder.previousBlockHead
                    ?: throw IllegalArgumentException("Spline line: previous block is null")
            blockMode = BlockMode.Append(previousBlockHead)

            if (previousBlockHead is PathLine) {
                val path = builder.planet.pathList.last().copy(
                        controlPoints = data
                )
                associatedPath = path
                builder.planet = builder.planet.copy(
                        pathList = builder.planet.pathList.dropLast(1) + path
                )
                return
            }
            if (previousBlockHead is StartPointLine) {
                val startPoint = builder.planet.startPoint
                        ?: throw IllegalArgumentException("Spline line: start point is null")
                associatedPath = previousBlockHead.data.path
                builder.planet = builder.planet.copy(
                        startPoint = startPoint.copy(
                                controlPoints = data
                        )
                )
                return
            }

            throw IllegalArgumentException("Spline line: previous block is ${previousBlockHead::class.simpleName}")
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj is List<*>) {
                return obj == data
            }

            return super.isAssociatedTo(obj)
        }

        companion object : Parser {
            override val name = "Spline line parser"
            private val REGEX =
                    """^#\s*(SPLINE|spline)\s?(?:\(\s*((?:[^\s,][^\n,]*?)?(?:\s*?,\s*?[^\s,][^\n,]*?)*)\s*\))?(?::\s?((?:-?\d+(?:\.\d+)?\s*?,\s*?(?:-?\d+(?:\.\d+)?))(?:\s*?\|\s*?(?:-?\d+(?:\.\d+)?\s*?,\s*?(?:-?\d+(?:\.\d+)?)))*)?)[ \t]*\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return SplineLine(line)
            }

            fun serialize(spline: List<Point>) = buildString {
                append("# spline: ")
                append(spline.joinToString(" | ") {
                    val x = it.left.toFixed(2)
                    val y = it.top.toFixed(2)
                    "$x,$y"
                })
            }

            fun create(spline: List<Point>) = createInstance(serialize(spline))
        }
    }

    class HiddenLine(override val line: String) : FileLine<Unit> {

        override val data = Unit

        var associatedPath: Path? = null

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            val previousBlockHead = builder.previousBlockHead
            if (previousBlockHead == null || previousBlockHead !is PathLine) {
                throw IllegalArgumentException("Hidden line: previous block is not a path")
            }
            blockMode = BlockMode.Append(previousBlockHead)

            val path = builder.planet.pathList.last().copy(
                    hidden = true
            )
            associatedPath = path
            builder.planet = builder.planet.copy(
                    pathList = builder.planet.pathList.dropLast(1) + path
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj is List<*>) {
                return obj == data
            }

            return super.isAssociatedTo(obj)
        }

        companion object : Parser {
            override val name = "Hidden line parser"
            private val REGEX =
                    """^#\s*(HIDDEN|hidden)\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return HiddenLine(line)
            }

            fun serialize() = "# hidden"

            fun create() = createInstance(serialize())
        }
    }

    class ErrorLine(override val line: String, override val data: String) : FileLine<String> {

        override lateinit var blockMode: BlockMode

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Skip(builder.previousBlockHead)
        }

        override fun isAssociatedTo(obj: Any) = false
    }

    // val REGEX_COMMENT =
    //         """^#\s*(?<type>COMMENT|comment)\s?(?:\(\s*(?<args>(?<x>-?\d+(?:\.\d+)?)\s*?,\s*?(?<y>-?\d+(?:\.\d+)?)(?:\s*?,\s*?[^\s,][^\n,]*)*)\s*\))?(?::\s?(?<data>\w[^\n]*?))?\s*(?:#.*?)?$""".toRegex()
}

private fun parseCoordinate(str: String): Coordinate {
    val values = str.split(',').map { it.trim().toInt() }
    return Coordinate(values[0], values[1])
}

private fun parseDirection(str: String) = when (str.toLowerCase()) {
    "n", "north" -> Direction.NORTH
    "e", "east" -> Direction.EAST
    "s", "south" -> Direction.SOUTH
    "w", "west" -> Direction.WEST
    else -> null

}

private fun serializeCoordinate(coordinate: Coordinate) = "${coordinate.x},${coordinate.y}"

private fun serializeDirection(direction: Direction) = when (direction) {
    Direction.NORTH -> "N"
    Direction.EAST -> "E"
    Direction.SOUTH -> "S"
    Direction.WEST -> "W"
}

private val parserList = listOf(
        FileLine.PathLine.Companion,
        FileLine.StartPointLine.Companion,
        FileLine.BluePointLine.Companion,
        FileLine.PathSelectLine.Companion,
        FileLine.TargetLine.Companion,
        FileLine.NameLine.Companion,
        FileLine.SplineLine.Companion,
        FileLine.HiddenLine.Companion,
        FileLine.BlankLine.Companion
)

fun parseLine(line: String): FileLine<*> {
    var lastName = ""
    try {
        for (parser in parserList) {
            lastName = parser.name

            if (parser.testLine(line)) {
                return parser.createInstance(line)
            }
        }
        return FileLine.UnknownLine(line)
    } catch (e: Exception) {
        return FileLine.ErrorLine(line, "Error in '$lastName': $e")
    }
}

fun Number.toFixed(places: Int): String {
    if (places == 0) {
        return toLong().toString()
    }

    val exp = 10.0.pow(places)
    val number = (round(toDouble() * exp) / exp).toString()

    val dotIndex = number.indexOf('.')

    if (dotIndex < 0) {
        return number + '.' + "0".repeat(places)
    }

    val missingPlaces = dotIndex + places - number.lastIndex

    if (missingPlaces == 0) {
        return number
    }

    if (missingPlaces > 0) {
        return number + "0".repeat(missingPlaces)
    }

    return number.dropLast(missingPlaces.absoluteValue)
}
