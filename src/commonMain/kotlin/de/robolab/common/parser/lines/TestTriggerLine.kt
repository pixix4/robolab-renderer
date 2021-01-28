package de.robolab.common.parser.lines

import de.robolab.common.parser.*
import de.robolab.common.testing.TestTrigger
import de.robolab.common.testing.serialize

class TestTriggerLine(override val line: String) : FileLine<TestTrigger> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        val signal = parseTestSignal(match.groupValues[2])!!
        val subscribable = parseSubscribableIdentifier(match.groupValues[3])

        TestTrigger(subscribable, signal)
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            testSuite = builder.planet.testSuite.copy(
                triggerList = builder.planet.testSuite.triggerList + data
            )
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is TestTrigger) {
            return obj == data
        }

        return super.isAssociatedTo(obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestTriggerLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Test trigger line parser"
        val REGEX =
            """^#\s*(trigger|TRIGGER)\s?(?:\(\s*([a-zA-Z]|[0-9]+)\s*\))(?::\s?(?:\s+(-?\d+,-?\d+(?:,[NSEW])?))?)?\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return TestTriggerLine(line)
        }

        fun serialize(trigger: TestTrigger): String {
            return "# trigger${trigger.triggered.serialize()}: ${trigger.subscribable.serialize()}"
        }

        fun create(goal: TestTrigger) = createInstance(serialize(goal))
    }
}
