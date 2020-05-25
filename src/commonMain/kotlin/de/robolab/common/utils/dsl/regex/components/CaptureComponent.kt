package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

class CaptureComponent(
    val name:String?=null,
    components: List<IRegexComponent> = emptyList()
) : SequentialComponentWithRegex(components) {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ) : Boolean {
        var result = style.startCapture(builder,name)
        result = sequentialPattern(builder, style, true) || result
        return style.endCapture(builder) || result
    }
}