package de.robolab.common.parser.lines

import de.robolab.common.parser.*
import de.robolab.common.planet.TestModifier
import de.robolab.common.planet.serialize
import de.robolab.common.planet.serializeType

class TestModifierLine(override val line: String) : FileLine<TestModifier> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        val type = match.groupValues[1].trim().toUpperCase().let { type ->
            TestModifier.Type.values().first { it.name == type }
        }
        val signal = parseTestSignal(match.groupValues[2])
        val (coordinate, direction) = parseTestCoordinate(match.groupValues[3])

        if (direction == null) {
            TestModifier.Coordinate(type, coordinate, signal)
        } else {
            TestModifier.Path(type, coordinate, direction, signal)
        }
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            testSuite = builder.planet.testSuite.copy(
                modifierList = builder.planet.testSuite.modifierList + data
            )
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is TestModifier) {
            return obj == data
        }

        return super.isAssociatedTo(obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestModifierLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Test modifier line parser"
        val REGEX =
            """^#\s*(allow|ALLOW|disallow|DISALLOW|skip|SKIP|unskip|UNSKIP)\s?(?:\(\s*([a-zA-Z]|[0-9]+)\s*\))?(?::\s?(?:\s+(-?\d+,-?\d+(?:,[NSEW])?))?)?\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return TestModifierLine(line)
        }

        fun serialize(modifier: TestModifier): String {
            return when (modifier) {
                is TestModifier.Coordinate ->
                    "# ${modifier.serializeType()}${modifier.signal.serialize()}: ${serializeCoordinate(modifier.coordinate)}"
                is TestModifier.Path ->
                    "# ${modifier.serializeType()}${modifier.signal.serialize()}: ${serializeCoordinate(modifier.coordinate)},${
                        serializeDirection(
                            modifier.direction
                        )
                    }"
            }
        }

        fun create(goal: TestModifier) = createInstance(serialize(goal))
    }
}
