package de.robolab.common.utils.dsl.regex

import de.robolab.common.utils.dsl.regex.components.ComponentWithRegex
import de.robolab.common.utils.dsl.regex.components.SequentialComponentWithRegex

class StyledRegexBuilder(val style: IRegexStyle) : SequentialComponentWithRegex() {

    fun build(): Regex{
        val builder = StringBuilder()
        toPattern(builder, style, true, this)
        return Regex(builder.toString())
    }

    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ) = super.toPattern(builder, this.style, isGrouped, this)
}