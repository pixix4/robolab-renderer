package de.robolab.common.testing

import de.robolab.common.planet.LookupPlanet
import de.robolab.common.planet.SubscribableIdentifier

abstract class TestSignalFlag(
    override val subscribable: SubscribableIdentifier<*>,
    private val activateSignals: Set<TestSignal>,
    deactivateSignals: Set<TestSignal>,
    private val defaultActive: Boolean
) : ISignalActor, ITestableSubscriber, ITestInit {
    override val signalGroups: List<TestSignal> = (activateSignals + deactivateSignals).toList()
    override fun ITestRun.onSignalTriggered(group: TestSignalGroup) {
        setActive(group.signal in activateSignals)
    }

    final override fun ITestRun.onTestableEntered() {
        if (isActive()) onTestableEnteredActive()
    }

    override fun ITestRun.init() {
        setActive(defaultActive)
    }

    protected abstract fun ITestRun.onTestableEnteredActive()

    companion object {
        fun create(
            type: TestFlagSetter.Type,
            activateSignals: Set<TestSignal>,
            deactivateSignals: Set<TestSignal>,
            subscribable: SubscribableIdentifier<*>,
            planet: LookupPlanet,
            defaultOverride: Boolean? = null
        ): TestSignalFlag = create(
            type,
            activateSignals,
            deactivateSignals,
            subscribable,
            defaultOverride ?: type.getDefault(subscribable, planet)
        )

        fun create(
            type: TestFlagSetter.Type,
            activateSignals: Set<TestSignal>,
            deactivateSignals: Set<TestSignal>,
            subscribable: SubscribableIdentifier<*>,
            default: Boolean
        ): TestSignalFlag = type.creator(activateSignals, deactivateSignals, subscribable, default)
    }

    class Disallow(
        disallowSignals: Set<TestSignal>,
        allowSignals: Set<TestSignal>,
        subscribable: SubscribableIdentifier<*>,
        defaultActive: Boolean,
    ) : TestSignalFlag(subscribable, disallowSignals, allowSignals, defaultActive) {
        override fun ITestRun.onTestableEnteredActive() {
            fail()
        }
    }

    class Skip(
        disallowSignals: Set<TestSignal>,
        allowSignals: Set<TestSignal>,
        subscribable: SubscribableIdentifier<*>,
        defaultActive: Boolean,
    ) : TestSignalFlag(subscribable, disallowSignals, allowSignals, defaultActive) {
        override fun ITestRun.onTestableEnteredActive() {
            skip()
        }
    }
}
