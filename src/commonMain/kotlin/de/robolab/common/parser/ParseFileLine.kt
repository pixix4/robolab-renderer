package de.robolab.common.parser

import de.robolab.common.planet.*
import de.robolab.common.utils.Point
import de.robolab.common.utils.withEntry
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

        object Unknown : BlockMode(null)
    }

    class BuildAccumulator(
        var planet: Planet = Planet.EMPTY.copy(version = PlanetVersion.FALLBACK),
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

        override var blockMode: BlockMode = BlockMode.Unknown

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

        override var blockMode: BlockMode = BlockMode.Unknown

        override fun buildPlanet(builder: BuildAccumulator) {
            val head = builder.previousBlockHead
            blockMode = if (head == null) {
                BlockMode.Head(null)
            } else {
                BlockMode.Append(head)
            }
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
                match.groupValues.getOrNull(6)?.split(" ")?.filter { it.isNotBlank() }?.map { parseCoordinate(it) }
                    ?.toSet()
                    ?: emptySet(),
                emptyList(),
                hidden = false,
                showDirectionArrow = false
            )
        }

        override var blockMode: BlockMode = BlockMode.Unknown

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
            val REGEX =
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

        override var blockMode: BlockMode = BlockMode.Unknown

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

            return obj.equalPoint(data)
        }

        companion object : Parser {
            override val name = "Start point line parser"
            val REGEX =
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

        override var blockMode: BlockMode = BlockMode.Unknown

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
            val REGEX =
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

        override var blockMode: BlockMode = BlockMode.Unknown

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
            val REGEX =
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

        override var blockMode: BlockMode = BlockMode.Unknown

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
            val REGEX =
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

        override val data = REGEX.matchEntire(line.trim())?.groupValues?.getOrNull(2) ?: ""

        override var blockMode: BlockMode = BlockMode.Unknown

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
            val REGEX =
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

    class VersionLine(override val line: String) : FileLine<PlanetVersion> {

        override val data = REGEX.matchEntire(line.trim())
            ?.groupValues
            ?.getOrNull(2)
            ?.toIntOrNull()
            ?.let {
                PlanetVersion.parse(it)
            } ?: PlanetVersion.FALLBACK

        override var blockMode: BlockMode = BlockMode.Unknown

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                version = data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj !is Int) return false

            return obj == data
        }

        companion object : Parser {
            override val name = "Version line parser"
            val REGEX =
                """^#\s*(VERSION|version)\s?(?::\s*(\d*))?\s*(?:#.*?)?${'$'}""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return VersionLine(line)
            }

            fun serialize(version: PlanetVersion) = "# version: ${version.version}"
            fun create(version: PlanetVersion) = createInstance(serialize(version))
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

        override var blockMode: BlockMode = BlockMode.Unknown

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
            val REGEX =
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

    class GroupingLine(override val line: String) : FileLine<Pair<Char, Set<Coordinate>>> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            match.groupValues[2].first() to match.groupValues[3].split('|').mapNotNull { p ->
                val h = p.split(',').map { it.trim().toInt() }
                if (h.size < 2) null else Coordinate(h[0], h[1])
            }.toSet()
        }

        override var blockMode: BlockMode = BlockMode.Unknown

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                senderGrouping = builder.planet.senderGrouping + (data.second to data.first)
            )
        }

        companion object : Parser {
            override val name = "Grouping line parser"
            val REGEX =
                """^#\s*(grouping|GROUPING)\s?(?::\s?([A-Z])\s?)(?::\s?((?:-?\d+,\s*?(?:-?\d+))(?:\s*?\|\s*?(?:-?\d+\s*?,\s*?(?:-?\d+)))*)?)[ \t]*\s*(?:#.*?)?${'$'}""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return GroupingLine(line)
            }

            fun serialize(set: Set<Coordinate>, char: Char) = buildString {
                append("# grouping: ")
                append(char)
                append(": ")
                append(set.joinToString(" | ") {(x, y) ->
                    "$x,$y"
                })
            }

            fun create(set: Set<Coordinate>, char: Char) = createInstance(serialize(set, char))
        }
    }

    class HiddenLine(override val line: String) : FileLine<Unit> {

        override val data = Unit

        var associatedPath: Path? = null

        override var blockMode: BlockMode = BlockMode.Unknown

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
            val REGEX =
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

    class TagLine(override val line: String): FileLine<Tag> {
        override val data = REGEX.matchEntire(line.trim())!!.let{ match->
            val key: String = match.groupValues[1]
            val value: String? = match.groupValues.getOrNull(2)
            return@let Tag(key, value?.split(',')?.map(String::trim))
        }

        override var blockMode: BlockMode = BlockMode.Unknown

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(tagMap = builder.planet.tagMap.withEntry(data.key to data.values.orEmpty()){
                _, a:List<String>,b:List<String> -> a+b
            })
        }

        companion object : Parser {
            override val name = "Tag line parser"
            val REGEX =
                """^#\s*\$(\w[\w-]*?)\s?(?::\s?(\w[\w-]*?(?:\s*?,\s*?\w[\w-]*?)*?))?\s*?(?:#.*?)?${'$'}""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return TagLine(line)
            }

            fun serialize(tag:Tag): String {
                val values = if (tag.values == null) {
                    ""
                } else {
                    ": ${tag.values.joinToString()}"
                }
                return "# $${tag.key}$values"
            }

            fun create(tag: Tag) = createInstance(serialize(tag))
        }
    }

    class CommentLine(override val line: String) : FileLine<Comment> {

        override val data = REGEX.matchEntire(line.trim())!!.let { match ->
            val h = match.groupValues[2].split(',').map { it.trim().toDouble() }
            val point = if (h.size < 2) Point.ZERO else Point(h[0], h[1])
            Comment(
                point,
                match.groupValues.getOrNull(5).toAlignment(),
                listOf(match.groupValues.getOrNull(6) ?: "")
            )
        }

        private fun String?.toAlignment(): Comment.Alignment {
            return when {
                this == null || this.isBlank() -> Comment.Alignment.CENTER
                "left".contains(this, true) -> {
                    Comment.Alignment.LEFT
                }
                "right".contains(this, true) -> {
                    Comment.Alignment.RIGHT
                }
                else -> Comment.Alignment.CENTER
            }
        }

        override var blockMode: BlockMode = BlockMode.Unknown

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Head(builder.previousBlockHead)
            builder.previousBlockHead = this
            builder.planet = builder.planet.copy(
                commentList = builder.planet.commentList + data
            )
        }

        override fun isAssociatedTo(obj: Any): Boolean {
            if (obj is Comment) {
                return obj.point == data.point && obj.lines.firstOrNull() == data.lines.firstOrNull()
            }

            return super.isAssociatedTo(obj)
        }

        companion object : Parser {
            override val name = "Comment line parser"
            val REGEX =
                """^#\s*(COMMENT|comment)\s?(?:\(\s*((-?\d+(?:\.\d+)?)\s*?,\s*?(-?\d+(?:\.\d+)?))\s*(?:,\s*([a-zA-Z]*))?\s*\))(?::\s?(\w[^\n]*?)?)?\s*(?:#.*?)?${'$'}""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return CommentLine(line)
            }

            fun serialize(comment: Comment): String {
                val alignment = if (comment.alignment == Comment.Alignment.CENTER) {
                    ""
                } else {
                    ",${comment.alignment.name.first()}"
                }
                return "# comment (${comment.point.x.toFixed(2)},${comment.point.y.toFixed(2)}$alignment): ${comment.lines.firstOrNull() ?: ""}"
            }

            fun create(comment: Comment) = createInstance(serialize(comment))
            fun createAll(comment: Comment) =
                listOf(createInstance(serialize(comment))) + comment.lines.drop(1).map { CommentSubLine.create(it) }
        }
    }

    class CommentSubLine(override val line: String) : FileLine<String> {

        override val data = REGEX.matchEntire(line.trim())?.let { match ->
            match.groupValues.getOrNull(2) ?: ""
        } ?: ""

        override var blockMode: BlockMode = BlockMode.Unknown

        override fun buildPlanet(builder: BuildAccumulator) {
            val previousBlockHead = builder.previousBlockHead
            if (previousBlockHead == null || previousBlockHead !is CommentLine) {
                throw IllegalArgumentException("Comment sub line: previous block is not a comment")
            }
            blockMode = BlockMode.Append(previousBlockHead)

            val lastComment = builder.planet.commentList.last()
            builder.planet = builder.planet.copy(
                commentList = builder.planet.commentList - lastComment + lastComment.copy(lines = lastComment.lines + data)
            )
        }

        companion object : Parser {
            override val name = "Comment line parser"
            val REGEX =
                """^#\s*(COMMENT|comment)\s?(?::\s?(\w[^\n]*?)?)?\s*(?:#.*?)?$""".toRegex()

            override fun testLine(line: String): Boolean {
                return REGEX.containsMatchIn(line)
            }

            override fun createInstance(line: String): FileLine<*> {
                return CommentSubLine(line)
            }

            fun serialize(comment: String) =
                "# comment: $comment"

            fun create(comment: String) = createInstance(serialize(comment))
        }
    }

    class ErrorLine(override val line: String, override val data: String) : FileLine<String> {

        override var blockMode: BlockMode = BlockMode.Unknown

        override fun buildPlanet(builder: BuildAccumulator) {
            blockMode = BlockMode.Skip(builder.previousBlockHead)
        }

        override fun isAssociatedTo(obj: Any) = false
    }
}

private fun parseCoordinate(str: String): Coordinate {
    val values = str.split(',').map { it.trim().toInt() }
    return Coordinate(values[0], values[1])
}

fun parseDirection(str: String) = when (str.toLowerCase()) {
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
    FileLine.PathLine,
    FileLine.StartPointLine,
    FileLine.BluePointLine,
    FileLine.PathSelectLine,
    FileLine.TargetLine,
    FileLine.NameLine,
    FileLine.VersionLine,
    FileLine.SplineLine,
    FileLine.HiddenLine,
    FileLine.TagLine,
    FileLine.GroupingLine,
    FileLine.CommentLine,
    FileLine.CommentSubLine,
    FileLine.BlankLine
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

fun isLineValid(line:String): Boolean {
    try {
        for (parser in parserList) {
            if (parser.testLine(line)) {
                return true
            }
        }
        return false
    } catch (e: Exception) {
        return false
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
