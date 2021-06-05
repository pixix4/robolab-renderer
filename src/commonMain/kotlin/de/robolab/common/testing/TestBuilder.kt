package de.robolab.common.testing

import de.robolab.common.planet.utils.LookupPlanet

fun LookupPlanet.buildTestPlanet(): TestPlanet = TODO() // planet.testSuite.buildPlanet(this)

fun TestSuite.buildPlanet(planet: LookupPlanet): TestPlanet {
    val (targetGoals, explorationGoals) = goals.partition { it is TestGoal.Target }

    val flagList = combineFlags(flagSetterList, planet)

    val signalGroups = createSignalGroups(
        taskList,
        triggerList,
        flagList
    )

    return TestPlanet(
        explorationGoals.map(TestGoal::coordinate).toSet(),
        targetGoals.map(TestGoal::coordinate).toSet(),
        taskList.groupBy { it.triggered }.mapValues { it.value.toSet() },
        triggerList,
        flagList,
        signalGroups
    )
}

fun combineFlags(setters: List<TestFlagSetter>, planet: LookupPlanet): List<TestSignalFlag> {
    return setters.groupBy(TestFlagSetter::type).flatMap { (type, setters) ->
        setters.groupBy(TestFlagSetter::subscribable).map { (subscribable, setters) ->
            val (activators, deactivators) = setters.partition(TestFlagSetter::value)
            val activatorSignals = activators.map(TestFlagSetter::signal)
            val deactivatorSignals = deactivators.map(TestFlagSetter::signal)
            val defaultActive = activatorSignals.contains(null)
            val defaultInactive = deactivatorSignals.contains(null)
            TestSignalFlag.create(
                type,
                activatorSignals.filterNotNull().toSet(),
                deactivatorSignals.filterNotNull().toSet(),
                subscribable,
                planet,
                when {
                    defaultActive && defaultInactive -> error("Flag $type on $subscribable defaults to both active and inactive")
                    defaultActive -> true
                    defaultInactive -> false
                    else -> null
                }
            )
        }
    }
}

fun createSignalGroups(
    tasks: List<TestTask>,
    triggers: List<TestTrigger>,
    flags: List<TestSignalFlag>,
): Map<TestSignal, TestSignalGroup> {

    val tasksBySignal = tasks.groupBy(TestTask::triggered)
    val triggersBySignal = triggers.groupBy(TestTrigger::triggered)
    val flagsBySignal = flags.flatMap { flag ->
        flag.actsOn.map {
            it to flag
        }
    }.groupBy(Pair<TestSignal, TestSignalFlag>::first, Pair<TestSignal, TestSignalFlag>::second)

    val signalSet: Set<TestSignal> = (tasks.mapNotNull { it.triggered }.toSet()
            + triggers.map { it.triggered }
            + flags.flatMap { it.actsOn })

    val unorderedSignals: List<TestSignal.Unordered> = signalSet.filterIsInstance<TestSignal.Unordered>()
    val orderedSignals: List<TestSignal.Ordered> = signalSet.filterIsInstance<TestSignal.Ordered>().sorted()


    fun createSignalGroup(signal: TestSignal, previous: TestSignalGroup? = null) = TestSignalGroup(
        signal,
        tasksBySignal.getOrElse(signal, ::emptyList),
        triggersBySignal.getOrElse(signal, ::emptyList),
        flagsBySignal.getOrElse(signal, ::emptyList),
        previous
    )


    return (unorderedSignals.associateWith<TestSignal, TestSignalGroup>(::createSignalGroup)
            + orderedSignals.scan(null as TestSignalGroup?) { prevGroup, signal ->
        createSignalGroup(
            signal,
            prevGroup
        )
    }.filterNotNull().associateBy(TestSignalGroup::signal))
}
