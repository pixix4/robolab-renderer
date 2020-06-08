package de.robolab.common.utils

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val label: String = ""
) {

    override fun toString(): String {
        val primary = "$major.$minor.$patch"

        return if (label.isBlank()) {
            primary
        } else {
            "$primary-$label"
        }
    }

    operator fun compareTo(other: Version): Int {
        return comparator.compare(this, other)
    }

    companion object {
        val UNKNOWN = Version(0, 0, 0, "")

        private val REGEX = """([0-9]+)(?:\.([0-9]+))?(?:\.([0-9]+))?(?:-(.+))?""".toRegex()

        private val comparator = compareBy<Version> { it.major }
            .thenBy { it.minor }
            .thenBy { it.patch }
            .thenBy { it.label }

        fun parse(version: String): Version {
            val match = REGEX.matchEntire(version) ?: return UNKNOWN

            return Version(
                match.groupValues[1].toIntOrNull() ?: return UNKNOWN,
                match.groupValues[2].toIntOrNull() ?: 0,
                match.groupValues[3].toIntOrNull() ?: 0,
                match.groupValues[4]
            )
        }
    }
}