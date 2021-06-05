package de.robolab.client.app.model.testing

import de.robolab.common.testing.*
import de.westermann.kobserve.base.ObservableMutableMap
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.map.observableMapOf
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.observeConst
import de.westermann.kobserve.property.property
import de.westermann.kobserve.set.observableSetOf

class SignalGroupEntry<T>(
    val signal: T,
    val tasks: ObservableMutableMap<TestTask, Boolean>,
    val triggers: ObservableSet<TestTrigger>,
    private val signalGroup: TestSignalGroup?,
) where T : TestSignal? {

    init {
        if (signal != signalGroup?.signal) throw IllegalArgumentException(
            "Provided signal $signal does not match the signal of the provided group: ${signalGroup?.signal}"
        )
    }

    val name: ObservableValue<String> = when (signal) {
        is TestSignal.Ordered -> signal.order.toString()
        is TestSignal.Unordered -> signal.label
        else -> ""
    }.observeConst()
    val isEmpty: ObservableValue<Boolean> = (tasks.isEmpty() && triggers.isEmpty()).observeConst()
    val completedTaskCount: ObservableValue<Int> = tasks.mapBinding { it.count(Map.Entry<*, Boolean>::value) }
    val totalTaskCount: ObservableValue<Int> = tasks.mapBinding(Map<*, *>::size)

    constructor(signal: T, signalGroup: TestSignalGroup) : this(
        signal,
        signalGroup.tasks.associateWithTo(observableMapOf()) { false },
        observableSetOf<TestTrigger>().apply { addAll(signalGroup.triggers) },
        signalGroup
    )

    constructor(
        signal: T, tasks: Set<TestTask>, triggers: Set<TestTrigger>, signalGroup: TestSignalGroup?
    ) : this(
        signal,
        tasks.associateWithTo(observableMapOf()) { false },
        observableSetOf<TestTrigger>().apply { addAll(triggers) },
        signalGroup
    )

    constructor(signal: T, planet: TestPlanet) : this(
        signal,
        planet.tasks.getValue(signal),
        if (signal != null)
            planet.signals.getValue(signal).triggers.toSet()
        else emptySet(),
        if (signal != null)
            planet.signals[signal]
        else null
    )


    val phase: ObservableProperty<TestSignalGroup.Phase> = property(TestSignalGroup.Phase.Pending)
    val collapsed: ObservableProperty<Boolean> = property(false)
    private val _collapsedDefault: ObservableValue<Boolean> = phase.mapBinding { it == TestSignalGroup.Phase.Started }

    fun rebindCollapsed() {
        collapsed.bind(_collapsedDefault)
    }

    init {
        rebindCollapsed()
    }

    val triggersVisible: ObservableValue<Boolean> = property(phase, tasks) {
        tasks.isEmpty() || phase.value == TestSignalGroup.Phase.Pending
    }

    fun updateFromState(state: TestState<*>) {
        for (entry in tasks.entries)
            entry.setValue(entry.key in state.completedTasks)
        phase.set(
            when {
                signalGroup != null -> state.signalPhases.getValue(signalGroup) //Might have been triggered by trigger
                !tasks.containsValue(false) -> TestSignalGroup.Phase.Complete
                !tasks.containsValue(true) -> TestSignalGroup.Phase.Pending
                else -> TestSignalGroup.Phase.Started
            }
        )
    }
}
