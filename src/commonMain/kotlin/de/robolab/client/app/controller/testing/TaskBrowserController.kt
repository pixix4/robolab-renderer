package de.robolab.client.app.controller.testing

import de.robolab.client.app.model.testing.SignalGroupEntry
import de.robolab.common.testing.TestPlanet
import de.robolab.common.testing.TestSignal
import de.robolab.common.testing.TestState
import de.westermann.kobserve.and
import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.or
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.set.observableSetOf

class TaskBrowserController(
    val planet: TestPlanet,
    val state: ObservableValue<TestState<*>>
) {

    val globalTasks: SignalGroupEntry<Nothing?> = SignalGroupEntry(null, planet)
    val orderedTasks: TaskAreaController<TestSignal.Ordered> = TaskAreaController(
        planet.orderedTasks.mapTo(observableSetOf()) { SignalGroupEntry(it.key, planet) }
    )
    val unorderedTasks: TaskAreaController<TestSignal.Unordered> = TaskAreaController(
        planet.unorderedTasks.mapTo(observableSetOf()) { SignalGroupEntry(it.key, planet) }
    )

    class TaskAreaController<T>(
        val tasks: ObservableSet<SignalGroupEntry<T>>
    ) where T : TestSignal {

        val isEmpty: ObservableValue<Boolean> =
            tasks.mapBinding(Collection<*>::isEmpty) or tasks.flatMapBinding {
                it.map(SignalGroupEntry<*>::isEmpty).reduce(ObservableValue<Boolean>::and)
            }
    }

}