package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

class GroupComponent(
    components:List<IRegexComponent> = emptyList()
) : SequentialComponentWithRegex(components) {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ) : Boolean {
        var result:Boolean = style.startGroup(builder)
        result = sequentialPattern(builder, style, true) || result
        result = style.endGroup(builder) || result
        return result
    }
}