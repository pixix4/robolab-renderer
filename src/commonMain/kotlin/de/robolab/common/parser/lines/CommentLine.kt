package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Comment
import de.robolab.common.utils.Point

class CommentLine(override val line: String) : FileLine<Comment> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        val h = match.groupValues[2].split(',').map { it.trim().toDouble() }
        val point = if (h.size < 2) Point.ZERO else Point(h[0], h[1])
        Comment(
            point,
            match.groupValues.getOrNull(5).toAlignment(),
            listOf(match.groupValues.getOrNull(6) ?: "")
        )
    }

    private fun String?.toAlignment(): Comment.Alignment {
        return when {
            this == null || this.isBlank() -> Comment.Alignment.CENTER
            "left".contains(this, true) -> {
                Comment.Alignment.LEFT
            }
            "right".contains(this, true) -> {
                Comment.Alignment.RIGHT
            }
            else -> Comment.Alignment.CENTER
        }
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            commentList = builder.planet.commentList + data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is Comment) {
            return obj.point == data.point && obj.lines.firstOrNull() == data.lines.firstOrNull()
        }

        return super.isAssociatedTo(obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommentLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Comment line parser"
        val REGEX =
            """^#\s*(COMMENT|comment)\s?(?:\(\s*((-?\d+(?:\.\d+)?)\s*?,\s*?(-?\d+(?:\.\d+)?))\s*(?:,\s*([a-zA-Z]*))?\s*\))(?::\s?(\w[^\n]*?)?)?\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return CommentLine(line)
        }

        fun serialize(comment: Comment): String {
            val alignment = if (comment.alignment == Comment.Alignment.CENTER) {
                ""
            } else {
                ",${comment.alignment.name.first()}"
            }
            return "# comment (${comment.point.x.toFixed(2)},${comment.point.y.toFixed(2)}$alignment): ${comment.lines.firstOrNull() ?: ""}"
        }

        fun create(comment: Comment) = createInstance(serialize(comment))
        fun createAll(comment: Comment) =
            listOf(createInstance(serialize(comment))) + comment.lines.drop(1).map { CommentSubLine.create(it) }
    }
}
