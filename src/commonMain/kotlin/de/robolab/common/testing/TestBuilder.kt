package de.robolab.common.testing

import de.robolab.common.planet.test.PlanetFlagType
import de.robolab.common.planet.test.PlanetSignalGroup
import de.robolab.common.planet.test.PlanetTestGoal
import de.robolab.common.planet.test.PlanetTestSuite
import de.robolab.common.planet.utils.LookupPlanet
import de.robolab.common.utils.partitionIsInstance2

fun LookupPlanet.buildTestPlanet(): TestPlanet = (planet.testSuite ?: PlanetTestSuite.EMPTY).buildPlanet(this)

fun PlanetTestSuite.buildPlanet(planet: LookupPlanet): TestPlanet {
    val (targetGoals, coordinateExplorationGoal, otherGoals) = goals.partitionIsInstance2<
            PlanetTestGoal.Target,
            PlanetTestGoal.ExploreCoordinate,
            PlanetTestGoal>()
    val exploreAnyGoals = otherGoals.filterIsInstance<PlanetTestGoal.Explore>()
    otherGoals.firstOrNull { it !is PlanetTestGoal.Explore }?.let {
        throw IllegalArgumentException("Unexpected test-goal: $it")
    }

    val globalTasks = this.globals.tasks

    val taskList: List<TestTask> = globalTasks.map { TestTask(it, null) } + this.signalGroups.flatMap { signal ->
        signal.tasks.map {
            TestTask(it, signal.testSignal)
        }
    }

    val triggerList: List<TestTrigger> = this.signalGroups.flatMap { signal ->
        signal.triggers.map {
            TestTrigger(it, signal.testSignal)
        }
    }

    val flagSetterList: List<TestFlagSetter> = this.globals.flags.map {
        TestFlagSetter(
            it.ref!!, null, it.type.testFlagSetterType, it.value
        )
    } + this.signalGroups.flatMap { signal ->
        signal.flags.map {
            TestFlagSetter(
                it.ref!!, signal.testSignal, it.type.testFlagSetterType, it.value
            )
        }
    }

    val flagList = combineFlags(flagSetterList, planet)

    val signalGroups = createSignalGroups(
        taskList,
        triggerList,
        flagList
    )

    return TestPlanet(
        (coordinateExplorationGoal.map(PlanetTestGoal.ExploreCoordinate::point) + exploreAnyGoals.map { it.point }).toSet(),
        targetGoals.map(PlanetTestGoal.Target::point).toSet(),
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

val PlanetSignalGroup.testSignal: TestSignal
    get() = when (this) {
        is PlanetSignalGroup.Unordered -> TestSignal.Unordered(label)
        is PlanetSignalGroup.Ordered -> TestSignal.Ordered(order.toInt())
    }

val PlanetFlagType.testFlagSetterType: TestFlagSetter.Type
    get() = when (this) {
        PlanetFlagType.Skip -> TestFlagSetter.Type.SKIP
        PlanetFlagType.Disallow -> TestFlagSetter.Type.DISALLOW
    }