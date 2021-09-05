package de.robolab.common.testing

import de.robolab.client.traverser.ITraverserState
import de.robolab.common.planet.test.PlanetTestGoal
import de.westermann.kobserve.base.*
import de.westermann.kobserve.map.asObservable
import de.westermann.kobserve.property.property
import de.westermann.kobserve.set.asObservable
import kotlin.math.sign

interface ITestRun {
    val planet: TestPlanet

    val state: TestState<*>

    val status: TestStatus

    val signalsByPhase: Map<TestSignalGroup.Phase, Set<TestSignalGroup>>

    fun completeTask(task: TestTask)

    fun taskCompleted(task: TestTask): Boolean

    fun fail(reason: String? = null): Nothing

    fun skip(reason: String? = null): Nothing

    fun completeGoal(exploration: Boolean)

    fun TestSignalFlag.isActive(): Boolean

    fun TestSignalFlag.setActive(active: Boolean)

    fun markSignal(signalGroup: TestSignalGroup, phase: TestSignalGroup.Phase = TestSignalGroup.Phase.Started): Boolean

    fun signalPhase(signalGroup: TestSignalGroup): TestSignalGroup.Phase

    fun queueAssert(block: () -> Unit)

    fun runAsserts()
}

open class TestRun<TS> protected constructor(
    final override val planet: TestPlanet,
    private val _signalPhases: MutableMap<TestSignalGroup, TestSignalGroup.Phase>,
    private val _completedTasks: MutableSet<TestTask>,
    private val _activeFlags: MutableSet<TestSignalFlag>,
    protected open var pStatus: TestStatus,
    protected open var pStatusMessage: String?,
    protected open var pAchievedGoalType: PlanetTestGoal.GoalType?,
    open var traverserState: TS,
    private val _signalsByPhase: MutableMap<TestSignalGroup.Phase, MutableSet<TestSignalGroup>> = _signalPhases.entries
        .groupBy({ it.value }, { it.key }).mapValuesTo(mutableMapOf()) { it.value.toMutableSet() }
) : ITestRun where TS : ITraverserState<TS> {
    constructor(planet: TestPlanet, state: TestState<TS>) : this(
        planet,
        state.signalPhases.toMutableMap(),
        state.completedTasks.toMutableSet(),
        state.activeFlags.toMutableSet(),
        state.status,
        state.statusMessage,
        state.achievedGoalType,
        state.traverserState,
        state.signalsByPhase.mapValuesTo(mutableMapOf()) { it.value.toMutableSet() }
    )

    override val status: TestStatus
        get() = pStatus
    override val signalsByPhase: Map<TestSignalGroup.Phase, Set<TestSignalGroup>> = _signalsByPhase

    override val state: TestState<TS>
        get() = TestState(
            _signalPhases.toMap(),
            _completedTasks.toSet(),
            _activeFlags.toSet(),
            pStatus,
            pStatusMessage,
            pAchievedGoalType,
            traverserState,
            _signalsByPhase.mapValues { it.value.toSet() }
        )

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

    override fun fail(reason: String?): Nothing {
        if (status != TestStatus.Running)
            throw IllegalStateException("Cannot transition from state $status into ${TestStatus.Failed}")
        pStatus = TestStatus.Failed
        pStatusMessage = reason
        throw TestRunFailedException(this, reason)
    }

    override fun skip(reason: String?): Nothing {
        if (status != TestStatus.Running)
            throw IllegalStateException("Cannot transition from state $status into ${TestStatus.Skipped}")
        pStatus = TestStatus.Skipped
        pStatusMessage = reason
        throw TestRunSkippedException(this, reason)
    }

    override fun completeTask(task: TestTask) {
        if (!_completedTasks.add(task)) return
        val signal = task.triggered ?: return
        val taskSet = planet.tasks[signal].orEmpty()
        if (_completedTasks.containsAll(taskSet))
            markSignal(planet.signals.getValue(signal), TestSignalGroup.Phase.Complete)
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

    override fun completeGoal(exploration: Boolean) {
        if (status != TestStatus.Running) throw IllegalStateException("Cannot enter goalAchieved-State from $status")
        if (!_completedTasks.containsAll(planet.tasks.values.flatten()))
            fail(
                "Attempted to complete goal with missing tasks: ${
                    planet.tasks.values.flatten().minus(_completedTasks).joinToString(prefix = "[", postfix = "]")
                }"
            )
        pAchievedGoalType = if (planet.explorationGoals.isEmpty() && planet.targetGoals.isEmpty()) {
            if (exploration) PlanetTestGoal.GoalType.Explore
            else PlanetTestGoal.GoalType.Target
        } else if (exploration) {
            when {
                planet.explorationGoals.contains(traverserState.location) -> PlanetTestGoal.GoalType.ExploreCoordinate
                planet.explorationGoals.contains(null) -> PlanetTestGoal.GoalType.Explore
                else -> fail("Completed exploration at location ${traverserState.location} which has no exploration-goal associated with it")
            }
        } else when {
            planet.targetGoals.contains(traverserState.location) -> PlanetTestGoal.GoalType.Target
            planet.targetGoals.contains(null) -> PlanetTestGoal.GoalType.Target
            else -> fail("Reached target at location ${traverserState.location} which has no target-goal associated with it")
        }
        pStatus = TestStatus.Success
    }
}

class ObservableTestRun<TS> private constructor(
    planet: TestPlanet,
    signalPhases: ObservableMutableMap<TestSignalGroup, TestSignalGroup.Phase>,
    completedTasks: ObservableMutableSet<TestTask>,
    activeFlags: ObservableMutableSet<TestSignalFlag>,
    status: TestStatus,
    statusMessage: String?,
    achievedGoalType: PlanetTestGoal.GoalType?,
    traverserState: TS,
    signalsByPhase: MutableMap<TestSignalGroup.Phase, MutableSet<TestSignalGroup>> = signalPhases.entries
        .groupBy({ it.value }, { it.key }).mapValuesTo(mutableMapOf()) { it.value.toMutableSet() }
) : TestRun<TS>(
    planet,
    signalPhases,
    completedTasks,
    activeFlags,
    status,
    statusMessage,
    achievedGoalType,
    traverserState,
    signalsByPhase
) where TS : ITraverserState<TS> {

    constructor(planet: TestPlanet, state: TestState<TS>) : this(
        planet,
        state.signalPhases.toMutableMap().asObservable(),
        state.completedTasks.toMutableSet().asObservable(),
        state.activeFlags.toMutableSet().asObservable(),
        state.status,
        state.statusMessage,
        state.achievedGoalType,
        state.traverserState,
        state.signalsByPhase.mapValuesTo(mutableMapOf()) { it.value.toMutableSet() }
    )

    private val _status = property(status)
    override var pStatus by _status
    private val _traverserState = property(traverserState)
    override var traverserState: TS by _traverserState
    private val _statusMessage = property(statusMessage)
    override var pStatusMessage: String? by _statusMessage
    private val _achievedGoalType = property<PlanetTestGoal.GoalType?>(null)
    override var pAchievedGoalType: PlanetTestGoal.GoalType? by _achievedGoalType

    val observableStatus: ObservableValue<TestStatus> = _status
    val observableStatusMessage: ObservableValue<String?> = _statusMessage
    val observableTraverserState: ObservableValue<TS> = _traverserState
    val observableAchievedGoalType: ObservableValue<PlanetTestGoal.GoalType?> = _achievedGoalType
    val signalPhases: ObservableMap<TestSignalGroup, TestSignalGroup.Phase> = signalPhases
    val completedTasks: ObservableSet<TestTask> = completedTasks
    val activeFlags: ObservableSet<TestSignalFlag> = activeFlags
}
