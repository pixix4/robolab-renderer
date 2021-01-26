package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.planet.Path

class HiddenLine(override val line: String) : FileLine<Unit> {

        override val data = Unit

        var associatedPath: Path? = null

        override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

        override fun buildPlanet(builder: FileLine.BuildAccumulator) {
            val previousBlockHead = builder.previousBlockHead
            if (previousBlockHead == null || previousBlockHead !is PathLine) {
                throw IllegalArgumentException("Hidden line: previous block is not a path")
            }
            blockMode = FileLine.BlockMode.Append(previousBlockHead)

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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HiddenLine) return false

            if (data != other.data) return false

            return true
        }

        override fun hashCode(): Int {
            return data.hashCode()
        }

        companion object : FileLine.Parser {
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
