package de.robolab.client.utils

fun String.removeCommon(base: String): String {
    return withIndex().dropWhile { it.value == base.getOrNull(it.index) }
        .joinToString(separator = "") { it.value.toString() }
}
