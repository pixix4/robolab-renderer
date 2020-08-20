package de.robolab.server.auth.parser

interface IStringParser<out T> {
    fun tryParse(line: String): T?
    fun parse(line: String): T
}

open class DelegateParser<out T>(val Parser: IStringParser<T>) : IStringParser<T> by Parser

abstract class RegexParser<T> : IStringParser<T> {
    protected abstract val regex: Regex

    override fun tryParse(line: String): T? {
        val match = regex.matchEntire(line)
        return parse(match ?: return null)
    }

    override fun parse(line: String): T {
        val match = regex.matchEntire(line)
        return parse(match ?: throw IllegalArgumentException("Line \"$line\" could not be parsed by $this (regex: \"\"\"$regex\"\"\")"))
    }

    override fun toString(): String {
        return "[RegexParser: ${this::class.js.name}]"
    }

    protected abstract fun parse(match: MatchResult): T
}

interface IChainParser<in R, S, out T> {
    fun trySplit(line: String): Pair<S, String>?
    fun split(line: String): Pair<S, String>
    fun tryMerge(state: S, result: R): T?
    fun merge(state: S, result: R): T
}

abstract class RegexChainParser<R, T> : IChainParser<R, MatchResult, T> {
    protected abstract val regex: Regex
    protected abstract val subParseGroupIndex: Int

    override fun trySplit(line: String): Pair<MatchResult, String>? {
        val match = regex.matchEntire(line) ?: return null
        return match to match.groupValues[subParseGroupIndex]
    }

    override fun split(line: String): Pair<MatchResult, String> {
        val match =
            regex.matchEntire(line) ?: throw IllegalArgumentException("Line \"$line\" could not be parsed by $this (subIndex: $subParseGroupIndex, regex: \"\"\"$regex\"\"\")")
        return match to match.groupValues[subParseGroupIndex]
    }

    override fun toString(): String {
        return "[RegexChainParser: ${this::class.js.name}]"
    }

    override fun tryMerge(state: MatchResult, result: R): T? = merge(state, result)
}

operator fun <T> IStringParser<T>.plus(other: IStringParser<T>): IStringParser<T> {
    return object : IStringParser<T> {
        override fun tryParse(line: String): T? = this@plus.tryParse(line) ?: other.tryParse(line)

        override fun parse(line: String): T = this@plus.tryParse(line) ?: other.parse(line)

        override fun toString(): String {
            return "[${this@plus} + $other]"
        }
    }
}

operator fun <R, S, T> IChainParser<R, S, T>.times(subParser: IStringParser<R>): IStringParser<T> {
    return object : IStringParser<T> {
        override fun tryParse(line: String): T? {
            val (state: S, subLine: String) = trySplit(line) ?: return null
            val subResult: R = subParser.tryParse(subLine) ?: return null
            return tryMerge(state, subResult)
        }

        override fun parse(line: String): T {
            val (state: S, subLine: String) = split(line)
            val subResult: R = subParser.parse(subLine)
            return merge(state, subResult)
        }

        override fun toString(): String {
            return "[${this@times} * $subParser]"
        }
    }
}