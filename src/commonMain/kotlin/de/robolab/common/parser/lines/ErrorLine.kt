package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine

class ErrorLine(override val line: String, override val data: String) : FileLine<String> {

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Skip(builder.previousBlockHead)
    }

    override fun isAssociatedTo(obj: Any) = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ErrorLine) return false

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
