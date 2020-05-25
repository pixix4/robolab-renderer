package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

abstract class SequentialComponentWithRegex(
    components: List<IRegexComponent> = emptyList()
) : ComponentWithRegex(components) {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ):Boolean = sequentialPattern(builder,style, isGrouped)

    protected fun sequentialPattern(builder: StringBuilder, style:IRegexStyle, isGrouped: Boolean): Boolean{
        return if(components.count() == 1)
            components.single().toPattern(builder, style, isGrouped, this)
        else{
            var hasContent = false
            for(component in components)
                hasContent=component.toPattern(builder, style, false, this) || hasContent
            hasContent
        }
    }
}