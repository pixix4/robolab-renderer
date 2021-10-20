package de.robolab.client.repl

import de.robolab.client.repl.base.ReplColor

fun IntRange.offset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun List<String>.autoComplete(prefix: String): List<ReplExecutor.AutoComplete> {
    return filter { it.startsWith(prefix) }.map {
        ReplExecutor.AutoComplete(
            it,
            it.removePrefix(prefix),
            ""
        )
    }
}

fun List<Pair<String, String>>.autoComplete(prefix: String): List<ReplExecutor.AutoComplete> {
    return filter { it.first.startsWith(prefix) }.map {
        ReplExecutor.AutoComplete(
            it.first,
            it.first.removePrefix(prefix),
            it.second
        )
    }
}

fun String.escapeIfNecessary(): String {
    val intern = if (this.contains('"')) this.replace("\"", "\\\"") else this
    return if (intern.contains(' ') || intern.contains('"')) "\"$intern\"" else intern
}
