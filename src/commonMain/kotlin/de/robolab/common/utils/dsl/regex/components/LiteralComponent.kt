package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

class LiteralComponent(val literal: String) : IRegexComponent {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ): Boolean {
        builder.append(Regex.escape(literal))
        return literal.isNotEmpty()
    }
}