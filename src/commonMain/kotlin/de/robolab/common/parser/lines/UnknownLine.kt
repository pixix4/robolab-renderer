package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine

class UnknownLine(override val line: String) : FileLine<Unit> {

    override val data = Unit

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        val head = builder.previousBlockHead
        blockMode = if (head == null) {
            FileLine.BlockMode.Head(null)
        } else {
            FileLine.BlockMode.Append(head)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownLine) return false

        if (line != other.line) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = line.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}
