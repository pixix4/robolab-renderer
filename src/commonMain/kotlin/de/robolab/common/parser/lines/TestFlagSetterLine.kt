package de.robolab.common.parser.lines

import de.robolab.common.parser.*
import de.robolab.common.testing.TestFlagSetter
import de.robolab.common.testing.serialize
import de.robolab.common.testing.serializeType

class TestFlagSetterLine(override val line: String) : FileLine<TestFlagSetter> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        val type = TestFlagSetter.Type.fromString(match.groupValues[1])
        val signal = parseTestSignal(match.groupValues[2])
        val subscribable = parseSubscribableIdentifier(match.groupValues[3])

        TestFlagSetter(subscribable, signal, type)
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            testSuite = builder.planet.testSuite.copy(
                flagSetterList = builder.planet.testSuite.flagSetterList + data
            )
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is TestFlagSetter) {
            return obj == data
        }

        return super.isAssociatedTo(obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestFlagSetterLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Test flag-setter line parser"
        val REGEX =
            """^#\s*(allow|ALLOW|disallow|DISALLOW|skip|SKIP|unskip|UNSKIP)\s?(?:\(\s*([a-zA-Z]|[0-9]+)\s*\))?(?::\s?(?:\s+(-?\d+,-?\d+(?:,[NSEW])?))?)?\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return TestFlagSetterLine(line)
        }

        fun serialize(flagSetter: TestFlagSetter): String {
            return "# ${flagSetter.serializeType()}${flagSetter.signal.serialize()}: ${flagSetter.subscribable.serialize()}"
        }

        fun create(goal: TestFlagSetter) = createInstance(serialize(goal))
    }
}
