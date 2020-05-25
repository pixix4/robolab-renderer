package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.CharacterClass
import de.robolab.common.utils.dsl.regex.IRegexStyle

class CharacterClassComponent(val negated:Boolean=false) : IRegexComponent{
    private val characters: MutableList<CharacterClassEntry> = mutableListOf()

    sealed class CharacterClassEntry{
        class LiteralCharacter(val character:Char) : CharacterClassEntry()
        class CharacterRange(val from:Char, val to:Char) : CharacterClassEntry()
        class CharacterSubclass(val identifier: Char): CharacterClassEntry()
    }

    operator fun<T> T.unaryPlus():T where T:CharacterClassEntry{ characters.add(this); return this }

    operator fun CharRange.unaryPlus(): CharacterClassEntry.CharacterRange
            = +CharacterClassEntry.CharacterRange(first, last)

    operator fun Char.unaryPlus(): CharacterClassEntry.LiteralCharacter
            = +CharacterClassEntry.LiteralCharacter(this)

    operator fun CharacterClass.unaryPlus()
            = +CharacterClassEntry.CharacterSubclass(identifier)

    fun subclass(identifier: Char)
            = +CharacterClassEntry.CharacterSubclass(identifier)

    operator fun CharacterClassEntry.LiteralCharacter.rangeTo(other: Char):CharacterClassEntry.CharacterRange{
        val index = characters.indexOf(this)
        if(index < 0 || index >= characters.size)
            throw IllegalArgumentException()
        val result = CharacterClassEntry.CharacterRange(this.character, other)
        characters[index] = result
        return result
    }

    override fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ): Boolean {
        style.beginCharacterClass(builder,negated)
        for(entry in characters)
            when(entry){
                is CharacterClassEntry.LiteralCharacter -> builder.append(Regex.escape(entry.character.toString()))
                is CharacterClassEntry.CharacterRange -> style.characterClassRange(builder, entry.from, entry.to)
                is CharacterClassEntry.CharacterSubclass -> builder.append("\\${entry.identifier}")
            }
        style.endCharacterClass(builder)
        return true
    }

}

