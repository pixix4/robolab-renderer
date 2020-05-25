package de.robolab.common.utils.dsl.regex

import de.robolab.common.utils.dsl.regex.components.ComponentWithRegex
import de.robolab.common.utils.dsl.regex.components.GroupComponent
import de.robolab.common.utils.dsl.regex.components.IRegexComponent

interface IRegexStyle {
    fun toGroupedPattern(builder:StringBuilder, component:IRegexComponent, nestedIn:ComponentWithRegex): Boolean{
        return GroupComponent(listOf(component)).toPattern(builder,this, false, nestedIn)
    }
    fun startGroup(builder:StringBuilder) : Boolean
    fun endGroup(builder:StringBuilder) : Boolean{
        builder.append(")")
        return true
    }
    fun startCapture(builder: StringBuilder, name:String?=null) : Boolean
    fun endCapture(builder:StringBuilder) : Boolean{
        builder.append(")")
        return true
    }
    fun multiple(builder:StringBuilder, minimum:Int, maximum:Int):Boolean
    fun multipleStar(builder:StringBuilder):Boolean
    fun multiplePlus(builder:StringBuilder):Boolean
    fun multipleMaybe(builder:StringBuilder):Boolean
    fun beginCharacterClass(builder: StringBuilder, negated: Boolean = false):Boolean{
        builder.append('[')
        if(negated)
            builder.append('^')
        return true
    }
    fun endCharacterClass(builder: StringBuilder): Boolean{
        builder.append(']')
        return true
    }
    fun characterClassRange(builder: StringBuilder, from:Char, to:Char):Boolean{
        builder.append("$from-$to")
        return true
    }
}

enum class RegexStyle : IRegexStyle {
    ECMAScript{
        override fun captureGroupNaming(name: String): String = "?<$name>"
        override fun multiple(minimum: Int, maximum: Int) = when{
            minimum == maximum -> "{$minimum}"
            maximum == Int.MAX_VALUE -> "{$minimum,}"
            else -> "{$minimum,$maximum}"
        }
    };

    protected open val nonCapturingGroupMarker: String = "?:"
    protected abstract fun captureGroupNaming(name:String):String
    protected abstract fun multiple(minimum: Int, maximum: Int): String

    override fun startCapture(builder: StringBuilder, name: String?) : Boolean
    {
        builder.append("(")
        if(name != null)
            builder.append(captureGroupNaming(name))
        return true
    }

    override fun startGroup(builder:StringBuilder) : Boolean {
        builder.append("(")
        builder.append(nonCapturingGroupMarker)
        return true
    }

    override fun multiple(builder: StringBuilder, minimum: Int, maximum: Int): Boolean {
        builder.append(multiple(minimum, maximum))
        return true
    }

    override fun multipleMaybe(builder: StringBuilder): Boolean {
        builder.append('?')
        return true
    }

    override fun multiplePlus(builder: StringBuilder): Boolean {
        builder.append('+')
        return true
    }

    override fun multipleStar(builder: StringBuilder): Boolean {
        builder.append('*')
        return true
    }
}