package de.robolab.common.testing

import de.robolab.common.planet.test.PlanetSubscribableRef

interface ITestPlanetBehaviour

interface ITestableSubscriber : ITestPlanetBehaviour {
    val subscribable: PlanetSubscribableRef
    fun ITestRun.onTestableEntered()
}

interface ITestSignalBehaviour

interface ISignalTrigger : ITestableSubscriber, ITestSignalBehaviour {
    val triggered: TestSignal?
    override fun ITestRun.onTestableEntered() {
        if (triggered == null)
            onTestableEntered(null)
        else
            onTestableEntered(planet.signals.getValue(triggered!!))
    }

    fun ITestRun.onTestableEntered(signal: TestSignalGroup?) {
        signal?.activate(this, allowMultipleUnordered = false)
    }
}

interface ISignalActor : ITestSignalBehaviour {
    val actsOn: List<TestSignal>
    fun ITestRun.onSignalTriggered(group: TestSignalGroup)
}
