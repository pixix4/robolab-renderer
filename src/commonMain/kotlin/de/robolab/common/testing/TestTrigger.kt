package de.robolab.common.testing

import de.robolab.common.planet.Planet
import de.robolab.common.planet.SubscribableIdentifier

data class TestTrigger(
    override val subscribable: SubscribableIdentifier<*>,
    override val triggered: TestSignal
) : ISignalTrigger {

    fun translate(delta: de.robolab.common.planet.Coordinate): TestTrigger =
        copy(subscribable = subscribable.translate(delta))

    fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestTrigger =
        copy(subscribable = subscribable.rotate(direction, origin))
    
    override fun ITestRun.onTestableEntered() {
        planet.signals.getValue(triggered).activate(this, allowMultipleUnordered = true)
    }

    override fun ITestRun.onTestableEntered(signal: TestSignalGroup?) {
        signal!!.activate(this, allowMultipleUnordered = true)
    }
}
