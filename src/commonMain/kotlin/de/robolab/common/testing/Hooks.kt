package de.robolab.common.testing

import de.robolab.common.planet.SubscribableIdentifier

interface ITestBehaviour {

}

interface ITestableSubscriber : ITestBehaviour {
    val subscribable: SubscribableIdentifier<*>
    fun ITestRun.onTestableEntered()
}

interface ISignalTrigger : ITestableSubscriber {
    val signal: TestSignal?
    override fun ITestRun.onTestableEntered() {
        if (signal == null)
            onTestableEntered(null)
        else
            onTestableEntered(planet.signals.getValue(signal!!))
    }

    fun ITestRun.onTestableEntered(signal: TestSignalGroup?) {
        signal?.activate(this, allowMultipleUnordered = false)
    }
}

interface ISignalActor : ITestBehaviour {
    val signalGroups: List<TestSignal>
    fun ITestRun.onSignalTriggered(group: TestSignalGroup)
}

interface ITestInit {
    fun ITestRun.init()
}