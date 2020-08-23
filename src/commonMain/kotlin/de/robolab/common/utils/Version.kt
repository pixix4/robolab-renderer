package de.robolab.common.utils

import kotlin.math.min

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String = "",
    val metadata: String = "",
) : Comparable<Version> {

    override fun toString(): String {
        return buildString {
            append("$major.$minor.$patch")

            if (preRelease.isNotBlank()) {
                append('-')
                append(preRelease)
            }

            if (metadata.isNotBlank()) {
                append('+')
                append(metadata)
            }
        }
    }

    override operator fun compareTo(other: Version): Int {
        return comparator.compare(this, other)
    }

    companion object {
        val UNKNOWN = Version(0, 0, 0, "", "")

        private val REGEX =
            """(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?""".toRegex()

        private val preReleaseComparator = object : Comparator<String> {
            override fun compare(a: String, b: String): Int {
                if (b.isBlank()) {
                    return -1
                }

                val aSplit = a.split(".")
                val bSplit = b.split(".")

                val minLength = min(aSplit.size, bSplit.size)

                for (i in 0 until minLength) {
                    val aString = aSplit[i]
                    val bString = bSplit[i]

                    val aNumber = aString.toIntOrNull()
                    val bNumber = bString.toIntOrNull()

                    val diff = if (aNumber == null) {
                        if (bNumber == null) {
                            compareValues(aString, bString)
                        } else {
                            1
                        }
                    } else {
                        if (bNumber == null) {
                            -1
                        } else {
                            aNumber - bNumber
                        }
                    }

                    if (diff != 0) {
                        return diff
                    }
                }
                return aSplit.size - bSplit.size
            }
        }

        private val comparator = compareBy<Version> { it.major }
            .thenBy { it.minor }
            .thenBy { it.patch }
            .thenBy(preReleaseComparator) { it.preRelease }

        fun parse(version: String): Version {
            val match = REGEX.matchEntire(version) ?: return UNKNOWN

            return Version(
                match.groupValues[1].toIntOrNull() ?: return UNKNOWN,
                match.groupValues[2].toIntOrNull() ?: 0,
                match.groupValues[3].toIntOrNull() ?: 0,
                match.groupValues[4],
                match.groupValues[5],
            )
        }
    }
}
