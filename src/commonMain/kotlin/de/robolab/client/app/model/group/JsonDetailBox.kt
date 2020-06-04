package de.robolab.client.app.model.group

import com.soywiz.klock.format
import de.robolab.client.app.model.IDetailBox
import de.robolab.client.communication.RobolabMessage

class JsonDetailBox(private val robolabMessage: RobolabMessage) : IDetailBox {

    val header: String
        get() = robolabMessage::class.simpleName ?: "Information"

    val from: String
        get() = robolabMessage.metadata.from.name.toLowerCase().capitalize()
    val group: String
        get() = robolabMessage.metadata.groupId
    val topic: String
        get() = robolabMessage.metadata.topic
    val time: String
        get() = InfoBarGroupInfo.TIME_FORMAT_DETAILED.format(robolabMessage.metadata.time)

    val details: String
        get() = robolabMessage.details.joinToString("\n") { (key, value) ->
            "$key: $value"
        }
    val rawMessage: String
        get() = formatRawMessage()

    private fun formatRawMessage(): String {
        val rawMessage = robolabMessage.metadata.rawMessage

        val builder = StringBuilder()

        var depth = 0
        var isString = false
        var stringChar = '"'
        var isEscape = false

        fun appendNewLine() {
            builder.append('\n')
            builder.append(" ".repeat(4 * depth))
        }

        for (char in rawMessage) {
            var isNextEscape = false
            when (char) {
                '{', '[' -> {
                    builder.append(char)
                    if (!isString) {
                        depth += 1
                        appendNewLine()
                    }
                }
                '}', ']' -> {
                    if (!isString) {
                        depth -= 1
                        appendNewLine()
                    }
                    builder.append(char)
                }
                ',' -> {
                    builder.append(',')
                    if (!isString) {
                        appendNewLine()
                    }
                }
                ':' -> {
                    builder.append(':')
                    if (!isString) {
                        builder.append(' ')
                    }
                }
                ' ' -> {
                    if (isString) {
                        builder.append(' ')
                    }
                }
                '\n' -> {
                }
                '\\' -> {
                    if (isEscape) {
                        builder.append('\\')
                    } else {
                        isNextEscape = true
                    }
                }
                '\'' -> {
                    builder.append('\'')
                    if (!isString) {
                        isString = true
                        stringChar = '\''
                    } else {
                        if (stringChar == '\'' && !isEscape) {
                            isString = false
                        }
                    }
                }
                '"' -> {
                    builder.append('"')
                    if (!isString) {
                        isString = true
                        stringChar = '"'
                    } else {
                        if (stringChar == '"' && !isEscape) {
                            isString = false
                        }
                    }
                }
                else -> {
                    builder.append(char)
                }
            }

            isEscape = isNextEscape
        }

        return builder.toString()
    }
}