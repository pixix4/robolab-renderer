package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine

class CommentSubLine(override val line: String) : FileLine<String> {

    override val data = REGEX.matchEntire(line.trim())?.let { match ->
        match.groupValues.getOrNull(2) ?: ""
    } ?: ""

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        val previousBlockHead = builder.previousBlockHead
        if (previousBlockHead == null || previousBlockHead !is CommentLine) {
            throw IllegalArgumentException("Comment sub line: previous block is not a comment")
        }
        blockMode = FileLine.BlockMode.Append(previousBlockHead)

        val lastComment = builder.planet.commentList.last()
        builder.planet = builder.planet.copy(
            commentList = builder.planet.commentList - lastComment + lastComment.copy(lines = lastComment.lines + data)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommentSubLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Comment line parser"
        val REGEX =
            """^#\s*(COMMENT|comment)\s?(?::\s?(\w[^\n]*?)?)?\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return CommentSubLine(line)
        }

        fun serialize(comment: String) =
            "# comment: $comment"

        fun create(comment: String) = createInstance(serialize(comment))
    }
}
