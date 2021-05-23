package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.parser.parseCoordinate
import de.robolab.common.parser.serializeCoordinate
import de.robolab.common.testing.TestGoal

class TestGoalLine(override val line: String) : FileLine<TestGoal> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        val type = match.groupValues[2].lowercase()
        val coordinate = match.groupValues[3]

        when {
            type == "target" -> {
                TestGoal.Target(parseCoordinate(coordinate))
            }
            coordinate.isEmpty() -> {
                TestGoal.Explore
            }
            else -> {
                TestGoal.ExploreCoordinate(parseCoordinate(coordinate))
            }
        }
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            testSuite = builder.planet.testSuite.copy(
                goals = builder.planet.testSuite.goals + data
            )
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is TestGoal) {
            return obj == data
        }

        return super.isAssociatedTo(obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestGoalLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Test goal line parser"
        val REGEX =
            """^#\s*(GOAL|goal)\s?(?::\s?(target|explore)(?:\s+(-?\d+,-?\d+))?)?\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return TestGoalLine(line)
        }

        fun serialize(goal: TestGoal): String {
            return when (goal) {
                is TestGoal.Target -> "# goal: target ${serializeCoordinate(goal.coordinate)}"
                TestGoal.Explore -> "# goal: explore"
                is TestGoal.ExploreCoordinate -> "# goal: explore ${serializeCoordinate(goal.coordinate)}"
            }
        }

        fun create(goal: TestGoal) = createInstance(serialize(goal))
    }
}
