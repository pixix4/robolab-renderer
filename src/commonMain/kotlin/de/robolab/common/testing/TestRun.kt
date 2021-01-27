package de.robolab.common.testing

import de.westermann.kobserve.base.*
import de.westermann.kobserve.map.observableMapOf
import de.westermann.kobserve.property.property
import de.westermann.kobserve.set.observableSetOf

interface ITestRun {
    val planet: TestPlanet

    val status: TestStatus

    val signalsByPhase: Map<TestSignalGroup.Phase, Set<TestSignalGroup>>

    fun completeTask(task: TestTask)

    fun taskCompleted(task: TestTask): Boolean

    fun fail(): Nothing

    fun skip(): Nothing

    fun TestSignalFlag.isActive(): Boolean

    fun TestSignalFlag.setActive(active: Boolean)

    fun markSignal(signalGroup: TestSignalGroup, phase: TestSignalGroup.Phase): Boolean

    fun signalPhase(signalGroup: TestSignalGroup): TestSignalGroup.Phase

    fun queueAssert(block: () -> Unit)

    fun runAsserts()

}

class RunFailedException(val run: ITestRun) : Exception()
class RunSkippedException(val run: ITestRun) : Exception()

open class TestRun protected constructor(
    final override val planet: TestPlanet,
    private val _signalPhases: MutableMap<TestSignalGroup, TestSignalGroup.Phase>,
    private val _completedTasks: MutableSet<TestTask>,
    private val _activeFlags: MutableSet<TestSignalFlag>,
) : ITestRun {
    constructor(planet: TestPlanet) : this(planet, mutableMapOf(), mutableSetOf(), mutableSetOf())

    init {
        planet.signals.values.associateWithTo(
            _signalPhases
        ) { TestSignalGroup.Phase.Pending }
    }

    protected open var pStatus = TestStatus.Pending
    override val status: TestStatus
        get() = pStatus
    private val _signalsByPhase = mutableMapOf(TestSignalGroup.Phase.Pending to planet.signals.values.toMutableSet())
    override val signalsByPhase: Map<TestSignalGroup.Phase, Set<TestSignalGroup>> = _signalsByPhase
    private val _pendingAsserts = mutableListOf<() -> Unit>()

    override fun markSignal(signalGroup: TestSignalGroup, phase: TestSignalGroup.Phase): Boolean {
        val oldPhase = _signalPhases.getValue(signalGroup)
        if (oldPhase >= phase) return false
        _signalsByPhase.getValue(oldPhase).remove(signalGroup)
        _signalsByPhase.getOrPut(phase, ::mutableSetOf).add(signalGroup)
        _signalPhases[signalGroup] = phase
        return true
    }

    override fun signalPhase(signalGroup: TestSignalGroup): TestSignalGroup.Phase = _signalPhases.getValue(signalGroup)

    override fun fail(): Nothing {
        if (status != TestStatus.Running)
            throw IllegalStateException("Cannot transition from state $status into ${TestStatus.Failed}")
        pStatus = TestStatus.Failed
        throw RunFailedException(this)
    }

    override fun skip(): Nothing {
        if (status != TestStatus.Running)
            throw IllegalStateException("Cannot transition from state $status into ${TestStatus.Skipped}")
        pStatus = TestStatus.Failed
        throw RunFailedException(this)
    }

    override fun completeTask(task: TestTask) {
        _completedTasks.add(task)
    }

    override fun taskCompleted(task: TestTask): Boolean = task in _completedTasks

    override fun TestSignalFlag.setActive(active: Boolean) {
        if (active)
            _activeFlags.add(this)
        else
            _activeFlags.remove(this)
    }

    override fun TestSignalFlag.isActive(): Boolean = this in _activeFlags

    override fun queueAssert(block: () -> Unit) {
        _pendingAsserts.add(block)
    }

    override fun runAsserts() {
        val asserts = _pendingAsserts.toList()
        asserts.forEach(::run)
        _pendingAsserts.removeAll(asserts)
    }
}

class ObservableTestRun(
    planet: TestPlanet,
    signalPhases: ObservableMutableMap<TestSignalGroup, TestSignalGroup.Phase> = observableMapOf(),
    completedTasks: ObservableMutableSet<TestTask> = observableSetOf(),
    activeFlags: ObservableMutableSet<TestSignalFlag> = observableSetOf(),
) : TestRun(
    planet,
    signalPhases,
    completedTasks,
    activeFlags
) {
    private val _status = property(TestStatus.Pending)
    override var pStatus by _status
    val observableStatus: ObservableValue<TestStatus> = _status
    val signalPhases: ObservableMap<TestSignalGroup, TestSignalGroup.Phase> = signalPhases
    val completedTasks: ObservableSet<TestTask> = completedTasks
    val activeFlags: ObservableSet<TestSignalFlag> = activeFlags
}
