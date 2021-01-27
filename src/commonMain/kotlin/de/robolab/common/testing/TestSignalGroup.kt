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
        if (test.markSignal(this, Phase.Started)) {
            subscribers.forEach(test::run)
            test.queueAssert {
                if (previousSignal != null && test.signalPhase(previousSignal) != Phase.Complete)
                    test.fail()
                if ((!allowMultipleUnordered) && (signal is TestSignal.Unordered)) {
                    if (test.signalsByPhase[Phase.Started]?.minus(this)
                            ?.any { it.signal is TestSignal.Unordered } == true
                    ) test.fail()
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
