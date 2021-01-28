package de.robolab.common.testing

import de.robolab.client.traverser.ITraverser
import de.robolab.client.traverser.ITraverserState
import de.robolab.client.traverser.TraverserState
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.LookupPlanet
import de.robolab.common.planet.letter
import de.robolab.common.utils.Logger
import de.robolab.common.utils.tree.SemiMutableTreeProvider
import de.robolab.common.utils.zipWithCopies
import io.ktor.util.date.*
import io.ktor.utils.io.*

class TestTraversal<TS>(val planet: TestPlanet, val traverser: ITraverser<TS>) :
    SemiMutableTreeProvider<TestState<TS>, TestRun<TS>>()
        where TS : ITraverserState<TS> {

    override val seed: TestState<TS> = TestState.empty(planet, traverser)

    override fun createMutable(immutable: TestState<TS>): TestRun<TS> = TestRun(planet, immutable)

    override fun extractImmutable(mutable: TestRun<TS>): TestState<TS> = mutable.state

    override fun canExpand(immutable: TestState<TS>): Boolean = immutable.status == TestStatus.Running

    override fun canExpand(mutable: TestRun<TS>): Boolean = mutable.status == TestStatus.Running

    override fun expand(mutable: TestRun<TS>): List<TestRun<TS>> {
        if (mutable.status != TestStatus.Running) throw IllegalArgumentException("Cannot expand non-running TestRun")
        return traverser.branch(mutable.traverserState)
            .zipWithCopies(mutable, { createMutable(extractImmutable(it)) }) { state, test ->
                try {
                    test.traverserState = state
                    when (test.traverserState.status) {
                        ITraverserState.Status.Running -> {
                            planet.triggerSubscribers(test, state)
                            test.runAsserts()
                        }
                        ITraverserState.Status.ExplorationComplete -> test.completeGoal(true)
                        ITraverserState.Status.TargetReached -> test.completeGoal(false)
                        ITraverserState.Status.TraverserCrashed -> test.fail(test.traverserState.statusInfo?.toString())
                        ITraverserState.Status.RobotCrashed -> test.fail(test.traverserState.statusInfo?.toString())
                    }
                } catch (_: TestRunSkippedException) {
                } catch (_: TestRunFailedException) {
                }
                test
            }.drop(1) //drop 1 because the 'mutable' input will be added to the front anyway
    }

    fun traverseToConsole() {
        val missingExplorations = planet.explorationGoals.toMutableSet()
        val missingTargets = planet.targetGoals.toMutableSet()
        for (s in this) {
            val ts = s.traverserState
            if (s.status == TestStatus.Success) {
                when (s.achievedGoalType) {
                    TestGoal.GoalType.Explore -> missingExplorations.remove(null)
                    TestGoal.GoalType.ExploreCoordinate -> {
                        missingExplorations.remove(null)
                        missingExplorations.remove(ts.location)
                    }
                    TestGoal.GoalType.Target -> {
                        missingTargets.remove(null)
                        missingTargets.remove(ts.location)
                    }
                    else -> throw IllegalArgumentException("TestStatus has been set to success but no goal has been achieved")
                }
            }
            val statusMessage: String = s.statusMessage?.let {
                ": $it"
            }.orEmpty()
            println(
                "trail: ${ts.getTrail().directions.joinToString("") { it.letter().toString() }}: " +
                        "${ts.location.toSimpleString()} -> ${ts.nextDirection?.letter() ?: "?"}: ${ts.status} / ${s.status}$statusMessage"
            )
        }
        if (missingExplorations.contains(null))
            println("No exploration has been completed")
        else if (missingExplorations.isNotEmpty())
            println("The following exploration-completes have not been reached: ${missingExplorations.joinToString { it!!.toSimpleString() }}")
        if (missingTargets.contains(null))
            println("No targets have been reached")
        else if (missingTargets.isNotEmpty())
            println("The following targets have not been reached: ${missingTargets.joinToString { it!!.toSimpleString() }}")
    }

    fun traverseToLogger(logger: Logger = Logger("Testing ${traverser.planet.planet.name}")) {
        val missingExplorations = planet.explorationGoals.toMutableSet()
        val missingTargets = planet.targetGoals.toMutableSet()
        for (s in this) {
            val ts = s.traverserState
            if (s.status == TestStatus.Success) {
                when (s.achievedGoalType) {
                    TestGoal.GoalType.Explore -> missingExplorations.remove(null)
                    TestGoal.GoalType.ExploreCoordinate -> {
                        missingExplorations.remove(null)
                        missingExplorations.remove(ts.location)
                    }
                    TestGoal.GoalType.Target -> {
                        missingTargets.remove(null)
                        missingTargets.remove(ts.location)
                    }
                    else -> throw IllegalArgumentException("TestStatus has been set to success but no goal has been achieved")
                }
            }
            logger.log(
                when {
                    (s.status == TestStatus.Running || s.status == TestStatus.Skipped) && ts.status == ITraverserState.Status.Running -> Logger.Level.DEBUG
                    s.status == TestStatus.Failed || ts.status == ITraverserState.Status.RobotCrashed || ts.status == ITraverserState.Status.TraverserCrashed -> Logger.Level.ERROR
                    s.status == TestStatus.Success && (ts.status == ITraverserState.Status.TargetReached || ts.status == ITraverserState.Status.ExplorationComplete) -> Logger.Level.INFO
                    else -> Logger.Level.WARN
                }
            ) {
                val statusMessage: String = s.statusMessage?.let {
                    ": $it"
                }.orEmpty()
                "${ts.getTrail().directions.joinToString("") { it.letter().toString() }}: " +
                        "${ts.location.toSimpleString()} -> ${ts.nextDirection?.letter() ?: "?"}: ${ts.status} / ${s.status}$statusMessage"
            }
        }
        if (missingExplorations.contains(null))
            logger.error { "No exploration has been completed" }
        else if (missingExplorations.isNotEmpty())
            logger.error { "The following exploration-completes have not been reached: ${missingExplorations.joinToString { it!!.toSimpleString() }}" }
        if (missingTargets.contains(null))
            logger.error { "No targets have been reached" }
        else if (missingTargets.isNotEmpty())
            logger.error { "The following targets have not been reached: ${missingTargets.joinToString { it!!.toSimpleString() }}" }
    }
}

fun <TS> LookupPlanet.testWith(traverser: ITraverser<TS>): TestTraversal<TS> where TS : ITraverserState<TS> =
    planet.testSuite.buildPlanet(this).testWith(traverser)
