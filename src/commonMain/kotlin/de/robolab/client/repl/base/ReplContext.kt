package de.robolab.client.repl.base

import de.robolab.client.app.model.base.MaterialIcon

enum class ReplColor {
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    GREY;
}

enum class ReplFileType {
    TEXT,
    BINARY,
}

interface IReplOutput {

    fun writeString(message: String, color: ReplColor? = null)
    fun writeIcon(icon: MaterialIcon, color: ReplColor? = null)
    fun writeFile(name: String, type: ReplFileType, content: suspend () -> String)
    fun writeAction(name: String, action: suspend () -> Unit)
    fun clearCurrentLine()

    fun write(message: Any?, color: ReplColor? = null) = writeString(message.toString(), color)
    fun writeln(message: Any?, color: ReplColor? = null) = writeString("${message.toString()}\n", color)
    fun writeln() = writeString("\n", null)
}

interface IReplParameterContext {

    fun <T : IReplCommandParameter> getGeneralParameter(parameter: ReplCommandParameterDescriptor<T>): List<T>


    fun <T : IReplCommandParameter> getParameter(parameter: ReplCommandDefaultParameterDescriptor<T>): T {
        return getGeneralParameter(parameter).singleOrNull()
            ?: throw IllegalArgumentException("Parameter ${parameter.name} is missing!")
    }

    fun <T : IReplCommandParameter> getParameter(parameter: ReplCommandOptionalParameterDescriptor<T>): T? {
        return getGeneralParameter(parameter).singleOrNull()
    }

    fun <T : IReplCommandParameter> getParameter(parameter: ReplCommandOptionalParameterDescriptor<T>, default: T): T =
        getParameter(parameter) ?: default

    fun <T : IReplCommandParameter> getParameter(parameter: ReplCommandVarargParameterDescriptor<T>): List<T> {
        return getGeneralParameter(parameter)
    }
}

interface IReplExecutionContext : IReplOutput, IReplParameterContext

object DummyReplOutput : IReplOutput {

    override fun writeString(message: String, color: ReplColor?) {
    }

    override fun writeIcon(icon: MaterialIcon, color: ReplColor?) {
    }

    override fun writeFile(name: String, type: ReplFileType, content: suspend () -> String) {
    }

    override fun writeAction(name: String, action: suspend () -> Unit) {
    }

    override fun clearCurrentLine() {
    }
}

class MultiReplOutput(private val outputList: List<IReplOutput>) : IReplOutput {

    override fun writeString(message: String, color: ReplColor?) {
        for (output in outputList) {
            output.writeString(message, color)
        }
    }

    override fun writeIcon(icon: MaterialIcon, color: ReplColor?) {
        for (output in outputList) {
            output.writeIcon(icon, color)
        }
    }

    override fun writeFile(name: String, type: ReplFileType, content: suspend () -> String) {
        for (output in outputList) {
            output.writeFile(name, type, content)
        }
    }

    override fun writeAction(name: String, action: suspend () -> Unit) {
        for (output in outputList) {
            output.writeAction(name, action)
        }
    }

    override fun clearCurrentLine() {
        for (output in outputList) {
            output.clearCurrentLine()
        }
    }
}
