package de.robolab.common.net.headers

import de.robolab.common.net.MIMEType
import de.robolab.common.utils.dsl.regex.CharacterClass
import de.robolab.common.utils.dsl.regex.regex

class ContentTypeHeader : Header {

    val mimeType: MIMEType
    val charset: String?
    val boundary: String?

    constructor(value: String) : super(ContentTypeHeader.name, value) {
        val mimeMatch: MatchResult =
            mimeRegex.find(value) ?: throw IllegalArgumentException("Could not parse MIME-Type from '$value'")
        val charsetMatch: MatchResult? = charsetRegex.find(value, mimeMatch.range.last + 1)
        val boundaryMatch: MatchResult? = boundaryRegex.find(value, mimeMatch.range.last + 1)

        mimeType = mimeMatch.groupValues[0].let {
            MIMEType.parse(it) ?: throw IllegalArgumentException("Unknown MIME-Type '$it'")
        }
        charset = charsetMatch?.groupValues?.get(0)
        boundary = boundaryMatch?.groupValues?.get(0)
    }

    constructor(mimeType: MIMEType, charset: String? = null, boundary: String? = null) : super(
        ContentTypeHeader.name,
        listOfNotNull(mimeType.primaryName, charset, boundary).joinToString("; ")
    ) {
        this.mimeType = mimeType
        this.charset = charset
        this.boundary = boundary
    }

    companion object {
        const val name: String = "content-type"

        private val mimeRegex: Regex = regex {
            capture {
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