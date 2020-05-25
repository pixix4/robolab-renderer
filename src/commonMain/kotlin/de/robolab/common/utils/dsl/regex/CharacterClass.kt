package de.robolab.common.utils.dsl.regex

enum class CharacterClass(val identifier: Char){
    WhiteSpace('s'),
    NonWhiteSpace('S'),
    Digit('d'),
    NonDigit('D'),
    WordCharacter('w'),
    NonWordCharacter('W'),
    VerticalWhitespace('v');
}