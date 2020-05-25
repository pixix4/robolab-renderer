package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

class CompiledComponent(val regex: Regex) :IRegexComponent{
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ) : Boolean {
        if(isGrouped){
            builder.append(regex.pattern)
            return regex.pattern.isNotEmpty()
        }
        else
            return style.toGroupedPattern(builder, this, nestedIn)
    }
}