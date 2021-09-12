package de.robolab.client.repl

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.repl.base.FileType
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplColor

object DummyReplOutput : IReplOutput {
    override fun writeString(message: String, color: ReplColor?) {
    }
    override fun writeIcon(icon: MaterialIcon, color: ReplColor?) {
    }
    override fun writeFile(name: String, type: FileType, content: suspend () -> String) {
    }
    override fun writeAction(name: String, action: suspend () -> Unit) {
    }
}
