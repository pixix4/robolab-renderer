package de.robolab.client.app.model.testing

import de.robolab.common.planet.test.PlanetSubscribableRef
import de.robolab.common.testing.*
import de.westermann.kobserve.base.ObservableMutableMap
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.map.observableMapOf
import de.westermann.kobserve.property.observeConst
import de.westermann.kobserve.property.property

class FlagEntry(
    val flag: TestSignalFlag,
    planet: TestPlanet,
    val active: ObservableProperty<Boolean> = property(flag.defaultActive),
) {

    val type: ObservableValue<TestFlagSetter.Type> = flag.type.observeConst()
    val location: ObservableValue<PlanetSubscribableRef> = flag.subscribable.observeConst()
    val defaultActive: ObservableValue<Boolean> = flag.defaultActive.observeConst()
    val onSignals: ObservableMutableMap<TestSignal, TestSignalGroup.Phase> =
        flag.activateSignals.associateWithTo(observableMapOf()) { TestSignalGroup.Phase.Pending }
    val offSignals: ObservableMutableMap<TestSignal, TestSignalGroup.Phase> =
        flag.deactivateSignals.associateWithTo(observableMapOf()) { TestSignalGroup.Phase.Pending }

    private val _signalMap = planet.signals

    fun updateFromState(state: TestState<*>) {
        active.set(flag in state.activeFlags)
        for (entry in onSignals)
            entry.setValue(state.signalPhases.getValue(_signalMap.getValue(entry.key)))
        for (entry in offSignals)
            entry.setValue(state.signalPhases.getValue(_signalMap.getValue(entry.key)))
    }
}
