package de.robolab.common.parser.lines

import de.robolab.common.parser.*
import de.robolab.common.planet.TestGoal
import de.robolab.common.planet.TestTask
import de.robolab.common.planet.serialize

class TestTaskLine(override val line: String) : FileLine<TestTask> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        val signal = parseTestSignal(match.groupValues[2])
        val (coordinate, direction) = parseTestCoordinate(match.groupValues[3])

        if (direction == null) {
            TestTask.Coordinate(coordinate, signal)
        } else {
            TestTask.Path(coordinate, direction, signal)
        }
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            testSuite = builder.planet.testSuite.copy(
                taskList = builder.planet.testSuite.taskList + data
            )
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is TestTask) {
            return obj == data
        }

        return super.isAssociatedTo(obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestTaskLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Test task line parser"
        val REGEX =
            """^#\s*(task|TASK)\s?(?:\(\s*([a-zA-Z]|[0-9]+)\s*\))?(?::\s?(?:\s+(-?\d+,-?\d+(?:,[NSEW])?))?)?\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return TestTaskLine(line)
        }

        fun serialize(task: TestTask): String {
            return when (task) {
                is TestTask.Coordinate ->
                    "# task${task.signal.serialize()}: ${serializeCoordinate(task.coordinate)}"
                is TestTask.Path ->
                    "# task${task.signal.serialize()}: ${serializeCoordinate(task.coordinate)},${serializeDirection(task.direction)}"
            }
        }

        fun create(goal: TestTask) = createInstance(serialize(goal))
    }
}
