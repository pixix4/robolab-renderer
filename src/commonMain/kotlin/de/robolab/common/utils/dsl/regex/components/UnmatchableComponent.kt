package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

object UnmatchableComponent : IRegexComponent {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ): Boolean{
        style.beginCharacterClass(builder,false)
        style.endCharacterClass(builder)
        return true
    }
}