package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

class MultipleComponent(
    val minimum:Int = 0,
    val maximum: Int = Int.MAX_VALUE,
    components: List<IRegexComponent> = emptyList(),
    val explicit: Boolean = false
) : SequentialComponentWithRegex(components) {
    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ): Boolean {
        if(nestedIn is MultipleComponent){
            if((!nestedIn.explicit) && nestedIn.minimum <= 0 && nestedIn.maximum == 1){
                return style.toGroupedPattern(builder,this, nestedIn)
            }
        }
        val wrapInGroup = components.size > 1
        val hasContent:Boolean = if(wrapInGroup){
            val tmpGroup = GroupComponent(components)
            tmpGroup.toPattern(builder, style, false, this)
        }else{
            sequentialPattern(builder, style, false)
        }
        when{
            !hasContent -> return false
            explicit -> style.multiple(builder, minimum, maximum)
            minimum <= 0 && maximum == 1 -> style.multipleMaybe(builder)
            minimum <= 0 && maximum == Int.MAX_VALUE -> style.multipleStar(builder)
            minimum == 1 && maximum == Int.MAX_VALUE -> style.multiplePlus(builder)
            else -> style.multiple(builder, minimum, maximum)
        }
        return true
    }
}