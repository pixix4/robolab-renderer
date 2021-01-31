package de.robolab.client.app.controller.testing

import de.robolab.client.app.model.testing.FlagEntry
import de.robolab.common.testing.TestPlanet
import de.robolab.common.testing.TestState
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf

class FlagBrowserController(
    val planet: TestPlanet,
    val state: ObservableValue<TestState<*>>
) {
    val flags: ObservableList<FlagEntry> = planet.flags.mapTo(observableListOf()) { FlagEntry(it, planet) }

    private fun updateFlags() {
        val state = state.value
        for (flag in flags)
            flag.updateFromState(state)
    }

    init {
        state.onChange += { updateFlags() }
        updateFlags()
    }
}