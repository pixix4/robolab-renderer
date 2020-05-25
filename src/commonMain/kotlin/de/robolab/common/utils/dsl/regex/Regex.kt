package de.robolab.common.utils.dsl.regex

expect val defaultStyle: IRegexStyle

fun regex(block: StyledRegexBuilder.()->Unit) : Regex = regex(defaultStyle, block)

fun regex(style: IRegexStyle, block:StyledRegexBuilder.()->Unit) : Regex{
    val builder = StyledRegexBuilder(style)
    block(builder)
    return builder.build()
}