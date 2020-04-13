package de.robolab.traverser

import de.robolab.planet.Direction
import de.robolab.planet.Path
import de.robolab.planet.Planet

open class Traverser<M, MS, N, NS>(val mothership: M, val navigator: N, val linkStates: Boolean = true) : ISeededBranchProvider<TraverserState<MS, NS>>, ITreeProvider<TraverserState<MS, NS>>
        where M : IMothership<MS>, MS : IMothershipState, N : INavigator<NS>, NS : INavigatorState {

    val planet: LookupPlanet = mothership.planet

    open val name: String = planet.planet.name + " (Traverser)"

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

    fun branch(node: TraverserState<MS, NS>): List<TraverserState<MS, NS>> = with(node) {

        if (!node.running)
            return@with emptyList()

        val leavingStates: List<NS> = getLeavingNavigatorStates(node)


        return@with leavingStates.map {
            when {
                it.explorationComplete -> return@map copy(
                        status = ITraverserState.Status.ExplorationComplete,
                        statusInfo = mothership.infoTestExplorationComplete(mothershipState),
                        navigatorState = it,
                        mothershipState = @Suppress("UNCHECKED_CAST")
                        (mothershipState.withAfterPoint as? MS) ?: mothershipState,
                        parent = if (linkStates) node else parent,
                        depth = depth + 1
                )
                it.targetReached -> return@map copy(
                        status = ITraverserState.Status.TargetReached,
                        statusInfo = mothership.infoTestTargetReached(mothershipState),
                        navigatorState = it,
                        mothershipState = @Suppress("UNCHECKED_CAST")
                        (mothershipState.withAfterPoint as? MS) ?: mothershipState,
                        parent = if (linkStates) node else parent,
                        depth = depth + 1)
                else -> return@map drivePath(pickDirection(node, it))
            }
        }
    }

    override val branchFunction: (TraverserState<MS, NS>) -> List<TraverserState<MS, NS>> = ::branch

    open fun drivePath(traverserState: TraverserState<MS, NS>, direction: Direction): TraverserState<MS, NS> =
            drivePath(traverserState, planet.getPath(traverserState.location, direction)!!)

    open fun drivePath(traverserState: TraverserState<MS, NS>, path: Path): TraverserState<MS, NS> {
        val newMothershipState: MS = mothership.drivePath(traverserState.mothershipState, path)
        return traverserState.copy(
                mothershipState = newMothershipState,
                navigatorState = navigator.drovePath(traverserState.navigatorState, path, newMothershipState.newPaths, newMothershipState.newTargets),
                parent = if (linkStates) traverserState else traverserState.parent,
                depth = traverserState.depth + 1
        )
    }

    open fun drivePath(traverserState: TraverserState<MS, NS>): TraverserState<MS, NS> = drivePath(traverserState, traverserState.nextDirection!!)

    open fun pickDirection(traverserState: TraverserState<MS, NS>, nextNavigatorState: NS): TraverserState<MS, NS> {
        val newMothershipState: MS = mothership.pickDirection(traverserState.mothershipState, nextNavigatorState.pickedDirection!!)
        return traverserState.copy(
                mothershipState = newMothershipState,
                navigatorState = navigator.leavingNode(nextNavigatorState, newMothershipState.forcedDirection
                        ?: newMothershipState.selectedDirection!!),
                parent = if (linkStates) traverserState else traverserState.parent,
                depth = traverserState.depth + 1
        )
    }

    override fun iterator(): TreeIterator<TraverserState<MS, NS>> = TreeIterator(this::branch, value)

    fun asTree(): ITreeProvider<TraverserState<MS, NS>> = TreeProvider(this::branch, value)

    override fun branch(): List<ITreeProvider<TraverserState<MS, NS>>> = children().map { TreeProvider(this::branch, it) }

    override fun children(): List<TraverserState<MS, NS>> = branch(value)
    override val seed: TraverserState<MS, NS> by lazy { TraverserState.getSeed(this) }
    override val value: TraverserState<MS, NS> by lazy { seed }
}

class DefaultTraverser(mothership: Mothership, navigator: Navigator, linkStates: Boolean = true) :
        Traverser<Mothership, MothershipState, Navigator, NavigatorState>(mothership, navigator, linkStates) {
    constructor(planet: Planet, linkStates: Boolean = true) : this(LookupPlanet(planet), linkStates)
    constructor(planet: LookupPlanet, linkStates: Boolean = true) : this(Mothership(planet), Navigator(planet), linkStates)
}
