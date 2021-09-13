package de.robolab.client.app.controller

import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.RobolabJson
import de.westermann.kobserve.event.EventHandler
import kotlinx.serialization.Serializable

object TerminalController {

    val onExecute = EventHandler<String>()

    fun execute(input: String) {
        commandHistory += input
        while (commandHistory.size > 100) {
            commandHistory.removeFirst()
        }
        saveHistory()

        onExecute.emit(input)
    }

    @Serializable
    data class TerminalHistory(
        val history: List<String>,
    )

    val commandHistory = mutableListOf<String>()

    private fun saveHistory() {
        PreferenceStorage.terminalHistory = RobolabJson.encodeToString(TerminalHistory.serializer(),
            TerminalHistory(commandHistory))
    }

    private fun loadHistory() {
        commandHistory.clear()
        try {
            commandHistory.addAll(RobolabJson.decodeFromString(TerminalHistory.serializer(),
                PreferenceStorage.terminalHistory).history)
        } catch (e: Exception) {
        }
    }

    init {
        loadHistory()
    }
}
