package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle

class AlternativeComponent(
    components: List<IRegexComponent> = emptyList(),
    val explicitGroup:Boolean = false
) : ComponentWithRegex(components) {

    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ) :Boolean {
        var result=false
        if(explicitGroup)
            result = style.startGroup(builder)
        result = when{
            components.isEmpty() -> false
            components.count() == 1 -> components.single().toPattern(builder, style, isGrouped, nestedIn)
            (!explicitGroup)&&(!isGrouped) && (nestedIn !is AlternativeComponent) ->
                style.toGroupedPattern(builder, this, nestedIn)
            else ->{
                var prevHadContent = components.first().toPattern(builder, style, false, this)
                for(component in components.drop(1)) {
                    if(prevHadContent){
                        builder.append("|")
                        result = true
                    }
                    prevHadContent=component.toPattern(builder, style, false, this)
                }
                prevHadContent
            }
        } || result
        if(explicitGroup)
            result = style.endGroup(builder) || result
        return result
    }

    infix fun or(other:IRegexComponent): AlternativeComponent = AlternativeComponent((components + other).toList())
    infix fun or(other:AlternativeComponent): AlternativeComponent = AlternativeComponent((components + other.components).toList())
}

infix fun IRegexComponent.or(other:IRegexComponent): AlternativeComponent = AlternativeComponent(mutableListOf(this,other))