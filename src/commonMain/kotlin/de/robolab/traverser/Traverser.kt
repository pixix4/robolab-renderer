package de.robolab.traverser

import de.robolab.planet.Direction
import de.robolab.planet.Path
import de.robolab.planet.Planet

open class Traverser<M, MS, N, NS>(val mothership: M, val navigator: N, val linkStates: Boolean = true) : Iterable<TraverserState<MS, NS>>
        where M : IMothership<MS>, MS : IMothershipState, N : INavigator<NS>, NS : INavigatorState {

    val planet: LookupPlanet = mothership.planet

    open fun getLeavingNavigatorStates(traverserState: TraverserState<MS, NS>): List<NS> = with(traverserState) {
        val temp: Pair<List<NS>, Boolean> = navigator.prepareLeaveNode(navigatorState, traverserState.location, traverserState.mothershipState.drivenPath)
        var leaveNodeStates: List<NS> = temp.first
        val exploring: Boolean = temp.second
        if (exploring) { //attempt reduction on forcedDirection
            val finishingStates: List<NS> = leaveNodeStates.filter { it.targetReached || it.explorationComplete }
            if (leaveNodeStates.size - finishingStates.size > 1) {
                val forcedDir: Direction? = mothership.peekForceDirection(mothershipState)
                if (forcedDir != null) {
                    val stateForForcedDir: List<NS> = leaveNodeStates.filter { it.pickedDirection == forcedDir }
                    if (stateForForcedDir.isNotEmpty())
                        leaveNodeStates = (finishingStates + stateForForcedDir).distinct()
                    else {
                        leaveNodeStates = (finishingStates + leaveNodeStates.filterNot(finishingStates::contains).first())
                    }
                }
            }
        }

        return@with leaveNodeStates
    }

    open fun branch(traverserState: TraverserState<MS, NS>): Sequence<TraverserState<MS, NS>> = with(traverserState) {

        if (traverserState.status != TraverserState.Status.Running) {
            if (traverserState.status == TraverserState.Status.TraverserCrashed)
                throw traverserState.statusCause as Throwable
            else
                return@with sequenceOf(traverserState.copy(status = TraverserState.Status.TraverserCrashed,
                        statusCause = if (traverserState.status == TraverserState.Status.RobotCrashed)
                            IllegalArgumentException("Cannot branch on crashed traverser", traverserState.statusCause as Throwable)
                        else
                            IllegalArgumentException("Cannot branch on not running traverser (${traverserState.status})"),
                        parent = if (linkStates) traverserState else parent
                ))
        }

        val leavingStates: Sequence<NS> = getLeavingNavigatorStates(traverserState).asSequence()

        return@with leavingStates.map {
            when {
                it.explorationComplete -> return@map copy(status = TraverserState.Status.ExplorationComplete, navigatorState = it)
                it.targetReached -> return@map copy(status = TraverserState.Status.TargetReached, navigatorState = it)
                else -> return@map drivePath(pickDirection(traverserState, it))
            }
        }
    }

    fun drivePath(traverserState: TraverserState<MS, NS>, direction: Direction): TraverserState<MS, NS> =
            drivePath(traverserState, planet.getPath(traverserState.location, direction)!!)

    fun drivePath(traverserState: TraverserState<MS, NS>, path: Path): TraverserState<MS, NS> {
        val newMothershipState: MS = mothership.drivePath(traverserState.mothershipState, path)
        return traverserState.copy(
                mothershipState = newMothershipState,
                navigatorState = navigator.drovePath(traverserState.navigatorState, path, newMothershipState.newPaths, newMothershipState.newTargets),
                parent = if (linkStates) traverserState else traverserState.parent
        )
    }

    fun drivePath(traverserState: TraverserState<MS, NS>): TraverserState<MS, NS> = drivePath(traverserState, traverserState.nextDirection!!)

    fun pickDirection(traverserState: TraverserState<MS, NS>, nextNavigatorState: NS): TraverserState<MS, NS> {
        val newMothershipState: MS = mothership.pickDirection(traverserState.mothershipState, nextNavigatorState.pickedDirection!!)
        return traverserState.copy(
                mothershipState = newMothershipState,
                navigatorState = navigator.leavingNode(nextNavigatorState, newMothershipState.forcedDirection
                        ?: newMothershipState.selectedDirection!!),
                parent = if (linkStates) traverserState else traverserState.parent
        )
    }

    override fun iterator(): TraverserIterator<M, MS, N, NS> = TraverserIterator(this)

}

class DefaultTraverser(mothership: Mothership, navigator: Navigator, linkStates: Boolean = true) :
        Traverser<Mothership, MothershipState, Navigator, NavigatorState>(mothership, navigator, linkStates) {
    constructor(planet: Planet, linkStates: Boolean = true) : this(LookupPlanet(planet), linkStates)
    constructor(planet: LookupPlanet, linkStates: Boolean = true) : this(Mothership(planet), Navigator(planet), linkStates)
}
