package de.robolab.common.planet

data class TestSuite(
    val goal: TestGoal?,
    val taskList: List<TestTask>,
    val triggerList: List<TestTrigger>,
    val modifierList: List<TestModifier>,
) {

    fun translate(delta: Coordinate): TestSuite {
        return TestSuite(
            goal?.translate(delta),
            taskList.map { it.translate(delta) },
            triggerList.map { it.translate(delta) },
            modifierList.map { it.translate(delta) }
        )
    }

    fun rotate(direction: Planet.RotateDirection, origin: Coordinate): TestSuite {
        return TestSuite(
            goal?.rotate(direction, origin),
            taskList.map { it.rotate(direction, origin) },
            triggerList.map { it.rotate(direction, origin) },
            modifierList.map { it.rotate(direction, origin) }
        )
    }

    companion object {
        val EMPTY = TestSuite(
            null,
            emptyList(),
            emptyList(),
            emptyList()
        )
    }
}
