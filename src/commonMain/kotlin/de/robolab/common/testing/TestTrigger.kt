package de.robolab.common.testing

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.test.PlanetSubscribableRef

data class TestTrigger(
    override val subscribable: PlanetSubscribableRef,
    override val triggered: TestSignal,
) : ISignalTrigger {

    fun translate(delta: PlanetPoint): TestTrigger =
        copy(subscribable = subscribable.translate(delta))

    fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestTrigger =
        copy(subscribable = subscribable.rotate(direction, origin))

    override fun ITestRun.onTestableEntered() {
        planet.signals.getValue(triggered).activate(this, allowMultipleUnordered = true)
    }

    override fun ITestRun.onTestableEntered(signal: TestSignalGroup?) {
        signal!!.activate(this, allowMultipleUnordered = true)
    }
}
