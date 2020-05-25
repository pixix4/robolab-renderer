package de.robolab.common.net.headers

import de.robolab.common.net.MIMEType
import de.robolab.common.utils.dsl.regex.CharacterClass
import de.robolab.common.utils.dsl.regex.regex

class ContentTypeHeader(value: String) : Header(name, value) {
    val mimeType: MIMEType
    val charset: String?
    val boundary: String?

    init{
        val mimeMatch: MatchResult = mimeRegex.find(value) ?: throw IllegalArgumentException("Could not parse MIME-Type from '$value'")
        mimeType = mimeMatch.groupValues[0].let {
            MIMEType.parse(it) ?: throw IllegalArgumentException("Unknown MIME-Type '$it'")
        }
        val charsetMatch: MatchResult? = charsetRegex.find(value,mimeMatch.range.last+1)
        charset = charsetMatch?.groupValues?.get(0)
        val boundaryMatch: MatchResult? = boundaryRegex.find(value, mimeMatch.range.last+1)
        boundary = boundaryMatch?.groupValues?.get(0)
    }

    companion object{
        const val name: String = "Content-Type"

        private val mimeRegex: Regex = regex {
            capture("class"){
                multiple {
                    characterClass {
                        +CharacterClass.WordCharacter
                        +'-'
                    }
                }
                maybe {
                    +"/"
                    multiple {
                        characterClass {
                            +CharacterClass.WordCharacter
                            +'-'
                        }
                    }
                }
            }
        }
        private val charsetRegex: Regex = regex {
            +"charset="
            capture {
                multiple {
                    characterClass {
                        +CharacterClass.WordCharacter
                        +'-'
                    }
                }
            }
        }
        private val boundaryRegex: Regex = regex {
            +"boundary="
            capture {
                multiple {
                    anyCharacter()
                }
                +CharacterClass.NonWhiteSpace
            }
        }
    }
}