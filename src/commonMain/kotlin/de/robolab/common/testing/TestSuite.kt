package de.robolab.common.testing

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint

data class TestSuite(
    val goals: List<TestGoal>,
    val taskList: List<TestTask>,
    val triggerList: List<TestTrigger>,
    val flagSetterList: List<TestFlagSetter>,
) {

    fun translate(delta: PlanetPoint): TestSuite {
        return TestSuite(
            goals.map { it.translate(delta) },
            taskList.map { it.translate(delta) },
            triggerList.map { it.translate(delta) },
            flagSetterList.map { it.translate(delta) }
        )
    }

    fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestSuite {
        return TestSuite(
            goals.map { it.rotate(direction, origin) },
            taskList.map { it.rotate(direction, origin) },
            triggerList.map { it.rotate(direction, origin) },
            flagSetterList.map { it.rotate(direction, origin) }
        )
    }

    operator fun plus(other: TestSuite): TestSuite = copy(
        goals = goals + other.goals,
        taskList = taskList + other.taskList,
        triggerList = triggerList + other.triggerList,
        flagSetterList = flagSetterList + other.flagSetterList
    )

    companion object {
        val EMPTY = TestSuite(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList()
        )
    }
}
