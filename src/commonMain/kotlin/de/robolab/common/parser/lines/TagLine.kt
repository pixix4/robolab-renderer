package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.planet.Tag
import de.robolab.common.utils.withEntry

class TagLine(override val line: String) : FileLine<Tag> {
    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        val key: String = match.groupValues[1]
        val value: String? = match.groupValues.getOrNull(2)
        return@let Tag(key, value?.split(',')?.map(String::trim))
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet =
            builder.planet.copy(tagMap = builder.planet.tagMap.withEntry(data.key to data.values.orEmpty()) { _, a: List<String>, b: List<String> ->
                a + b
            })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TagLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Tag line parser"
        val REGEX =
            """^#\s*\$(\w[\w-]*?)\s?(?::\s?(\w[\w-]*?(?:\s*?,\s*?\w[\w-]*?)*?))?\s*?(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return TagLine(line)
        }

        fun serialize(tag: Tag): String {
            val values = if (tag.values == null) {
                ""
            } else {
                ": ${tag.values.joinToString()}"
            }
            return "# $${tag.key}$values"
        }

        fun create(tag: Tag) = createInstance(serialize(tag))
    }
}
