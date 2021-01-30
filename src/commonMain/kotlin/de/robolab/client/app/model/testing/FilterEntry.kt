package de.robolab.client.app.model.testing

import de.robolab.common.testing.TestGoal
import de.robolab.common.testing.TestStatus
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.observeConst
import de.westermann.kobserve.property.property

open class FilterEntry<T>(
    val content: ObservableValue<T>,
    val active: ObservableProperty<Boolean>,
    val matchingCount: ObservableProperty<Int>
) {

    class GoalFilterEntry(
        content: ObservableValue<TestGoal>,
        active: ObservableProperty<Boolean>,
        matchingCount: ObservableProperty<Int>
    ) : FilterEntry<TestGoal>(content, active, matchingCount) {
        constructor(content: TestGoal) : this(content.observeConst(), property(false), property(0))
    }

    class StatusFilterEntry(
        content: ObservableValue<TestStatus>,
        active: ObservableProperty<Boolean>,
        matchingCount: ObservableProperty<Int>
    ) : FilterEntry<TestStatus>(content, active, matchingCount) {
        constructor(content: TestStatus) : this(content.observeConst(), property(false), property(0))
    }
}