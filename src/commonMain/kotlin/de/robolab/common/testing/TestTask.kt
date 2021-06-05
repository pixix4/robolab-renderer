package de.robolab.common.testing

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.test.PlanetSubscribableRef

data class TestTask(
    override val subscribable: PlanetSubscribableRef,
    override val triggered: TestSignal?,
) : ISignalTrigger {

    fun translate(delta: PlanetPoint): TestTask =
        copy(subscribable = subscribable.translate(delta))

    fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestTask =
        copy(subscribable = subscribable.rotate(direction, origin))

    override fun ITestRun.onTestableEntered(signal: TestSignalGroup?) {
        signal?.activate(this)
        completeTask(this@TestTask)
    }
}
