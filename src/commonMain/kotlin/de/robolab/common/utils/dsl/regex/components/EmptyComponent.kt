package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

object EmptyComponent : IRegexComponent {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ) : Boolean{
        var result: Boolean = style.startGroup(builder)
        result = style.endGroup(builder) || result
        return result
    }
}

object EpsilonComponent : IRegexComponent {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ): Boolean = false

}