package de.robolab.common.testing

class TestSignalGroup(
    val signal: TestSignal,
    val tasks: List<TestTask>,
    val triggers: List<TestTrigger>,
    val flags: List<TestSignalFlag>,
    val previousSignal: TestSignalGroup?,
    otherSubscribers: List<ITestRun.() -> Unit> = emptyList()
) {

    private val subscribers: List<ITestRun.() -> Unit> =
        (flags).map<ISignalActor, ITestRun.() -> Unit> { flag ->
            {
                with(flag) {
                    onSignalTriggered(this@TestSignalGroup)
                }
            }
        } + otherSubscribers


    fun activate(test: ITestRun, allowMultipleUnordered: Boolean = false) {
        if (test.markSignal(this)) {
            subscribers.forEach(test::run)
            test.queueAssert {
                if (previousSignal != null && test.signalPhase(previousSignal) != Phase.Complete)
                    test.fail(
                        "Cannot trigger signal $signal until previous signal (${previousSignal.signal}) is complete; current phase: ${
                            test.signalPhase(previousSignal)
                        }"
                    )
                if ((!allowMultipleUnordered) && (signal is TestSignal.Unordered)) {
                    val otherUnorderedSignals = test.signalsByPhase[Phase.Started]?.minus(this)?.map {
                        it.signal
                    }?.filterIsInstance<TestSignal.Unordered>()
                    if (otherUnorderedSignals?.any() == true) test.fail(
                        "Cannot trigger $signal until all started unordered signals are completed: ${
                            otherUnorderedSignals.joinToString { "\"${it.label}\"" }
                        }"
                    )
                }
            }
        }
    }

    enum class Phase {
        Pending,
        Started,
        Complete
    }
}
