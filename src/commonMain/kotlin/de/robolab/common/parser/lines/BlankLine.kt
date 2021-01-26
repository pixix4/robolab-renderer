package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine

class BlankLine : FileLine<Unit> {
    override val line = ""
    override val data = Unit

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Skip(builder.previousBlockHead)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlankLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }


    companion object : FileLine.Parser {
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
