package de.robolab.common.parser

import de.robolab.common.parser.lines.*
import de.robolab.common.planet.*
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
}

fun parseCoordinate(str: String): Coordinate {
    val values = str.split(',').take(2).map { it.trim().toInt() }
    return Coordinate(values[0], values[1])
}

fun parseTestCoordinate(str: String): Pair<Coordinate, Direction?> {
    val split = str.split(',')
    val values = split.take(2).map { it.trim().toInt() }
    return Coordinate(values[0], values[1]) to split.getOrNull(2)?.let { parseDirection(it) }
}

fun parseTestSignal(str: String): TestSignal? {
    if (str.isBlank()) return null

    val int = str.trim().toIntOrNull()
    if (int != null) {
        return TestSignal.Ordered(int)
    }

    return TestSignal.Unordered(str.trim())
}

fun parseDirection(str: String) = when (str.toLowerCase()) {
    "n", "north" -> Direction.NORTH
    "e", "east" -> Direction.EAST
    "s", "south" -> Direction.SOUTH
    "w", "west" -> Direction.WEST
    else -> null
}

fun serializeCoordinate(coordinate: Coordinate) = "${coordinate.x},${coordinate.y}"

fun serializeDirection(direction: Direction) = when (direction) {
    Direction.NORTH -> "N"
    Direction.EAST -> "E"
    Direction.SOUTH -> "S"
    Direction.WEST -> "W"
}

private val parserList = listOf(
    PathLine,
    StartPointLine,
    BluePointLine,
    PathSelectLine,
    TargetLine,
    NameLine,
    VersionLine,
    SplineLine,
    HiddenLine,
    TagLine,
    GroupingLine,
    CommentLine,
    CommentSubLine,
    TestGoalLine,
    TestTaskLine,
    TestTriggerLine,
    TestModifierLine,
    BlankLine
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
        return UnknownLine(line)
    } catch (e: Exception) {
        return ErrorLine(line, "Error in '$lastName': $e")
    }
}

fun isLineValid(line: String): Boolean {
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
