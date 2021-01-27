package de.robolab.common.testing

import de.robolab.common.planet.Planet
import de.robolab.common.planet.SubscribableIdentifier

data class TestTask(
    override val subscribable: SubscribableIdentifier<*>,
    override val signal: TestSignal?
) : ISignalTrigger {

    fun translate(delta: de.robolab.common.planet.Coordinate): TestTask =
        copy(subscribable = subscribable.translate(delta))

    fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestTask =
        copy(subscribable = subscribable.rotate(direction, origin))

    override fun ITestRun.onTestableEntered(signal: TestSignalGroup?) {
        signal?.activate(this)
        completeTask(this@TestTask)
    }
}
