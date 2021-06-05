package de.robolab.common.planet.utils

abstract class TagQuery(val tagName:String, val matchMissing: Boolean = false) {
    abstract fun matches(value: String): Boolean
    open fun matches(values: List<String>): Boolean = values.any(this::matches)

    companion object{
        fun fromOperator(tagName: String, operator: String, argument: String): TagQuery = when(operator){
            ":"-> Contains(tagName, argument)
            "="-> Equals(tagName, argument)
            "<"-> LessThan(tagName, argument)
            ">"-> GreaterThan(tagName, argument)
            else -> throw IllegalArgumentException("Unknown operator: \"$operator\"")
        }
    }

    class Contains(tagName: String, val value: String) : TagQuery(tagName){
        override fun matches(value: String): Boolean = value.contains(this.value, ignoreCase = true)
        override fun toString(): String = "[$tagName CONTAINS $value]"
    }

    class Equals(tagName: String, val value:String) : TagQuery(tagName){
        override fun matches(value: String): Boolean = value.equals(this.value, ignoreCase = true)
        override fun toString(): String = "[$tagName EQUALS $value]"
    }

    class LessThan(tagName: String, val value:String) : TagQuery(tagName){
        override fun matches(value: String): Boolean {
            return (this.value.toDoubleOrNull() ?: return false) < (value.toDoubleOrNull() ?: return false)
        }
        override fun toString(): String = "[$tagName LESSTHAN $value]"
    }

    class GreaterThan(tagName: String, val value:String) : TagQuery(tagName){
        override fun matches(value: String): Boolean {
            return (this.value.toDoubleOrNull() ?: return false) < (value.toDoubleOrNull() ?: return false)
        }
        override fun toString(): String = "[$tagName GREATERTHAN $value]"
    }

    class HasTag(tagName: String): TagQuery(tagName, false){
        override fun matches(value: String) = true
        override fun matches(values: List<String>): Boolean = true
        override fun toString(): String = "[HASTAG $tagName]"
    }
}