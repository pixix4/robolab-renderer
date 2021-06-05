package de.robolab.client.traverser

import de.robolab.common.planet.*
import de.robolab.common.planet.utils.LookupPlanet
import de.robolab.common.utils.tree.ISeededBranchProvider
import de.robolab.common.utils.tree.ITreeProvider
import de.robolab.common.utils.tree.TreeIterator
import de.robolab.common.utils.tree.TreeProvider

interface ITraverser<TS> : ISeededBranchProvider<TS>, ITreeProvider<TS> where TS : ITraverserState<TS> {
    val planet: LookupPlanet
    val mothership: IMothership<*>
    val navigator: INavigator<*>
    fun branch(node: TS): List<TS>
    override val branchFunction: (TS) -> List<TS>
        get() = ::branch

    fun asTree(): ITreeProvider<TS> = TreeProvider(this::branch, value)
    override fun iterator(): TreeIterator<TS> = TreeIterator(this::branch, value)

    override fun branch(): List<ITreeProvider<TS>> =
        children().map { TreeProvider(this::branch, it) }

    override fun children(): List<TS> = branch(value)
}


open class Traverser<M, MS, N, NS>(
    final override val mothership: M,
    override val navigator: N,
    val linkStates: Boolean = true
) :
    ITraverser<TraverserState<MS, NS>>
        where M : IMothership<MS>, MS : IMothershipState, N : INavigator<NS>, NS : INavigatorState {

    final override val planet: LookupPlanet = mothership.planet

    open val name: String = planet.planet.name + " (Traverser)"

    open fun getLeavingNavigatorStates(traverserState: TraverserState<MS, NS>): List<NS> = with(traverserState) {
        val temp: Pair<List<NS>, Boolean> = navigator.prepareLeaveNode(
            navigatorState,
            traverserState.location,
            traverserState.mothershipState.drivenPath
        )
        var leaveNodeStates: List<NS> = temp.first
        val exploring: Boolean = temp.second
        if (exploring) { //attempt reduction on forcedDirection
            val finishingStates: List<NS> = leaveNodeStates.filter { it.targetReached || it.explorationComplete }
            if (leaveNodeStates.size - finishingStates.size > 1) {
                val forcedDir: PlanetDirection? = mothership.peekForceDirection(mothershipState)
                if (forcedDir != null) {
                    val stateForForcedDir: List<NS> = leaveNodeStates.filter { it.pickedDirection == forcedDir }
                    leaveNodeStates = if (stateForForcedDir.isNotEmpty())
                        (finishingStates + stateForForcedDir).distinct()
                    else {
                        (finishingStates + leaveNodeStates.filterNot(finishingStates::contains).first())
                    }
                }
            }
        }

        return@with leaveNodeStates
    }

    override fun branch(node: TraverserState<MS, NS>): List<TraverserState<MS, NS>> = with(node) {

        if (!node.running)
            return@with emptyList()

        if (node.beforePoint) {
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
                        depth = depth + 1
                    )

                    else -> return@map pickDirection(node, it)
                }
            }
        } else {
            return@with listOf(drivePath(node))
        }
    }

    open fun drivePath(traverserState: TraverserState<MS, NS>, direction: PlanetDirection): TraverserState<MS, NS> =
        drivePath(traverserState, planet.getPath(traverserState.location, direction)!!)

    open fun drivePath(traverserState: TraverserState<MS, NS>, path: PlanetPath): TraverserState<MS, NS> {
        val newMothershipState: MS = mothership.drivePath(traverserState.mothershipState, path)
        return traverserState.copy(
            mothershipState = newMothershipState,
            navigatorState = navigator.drovePath(
                traverserState.navigatorState,
                path,
                newMothershipState.newPaths,
                newMothershipState.newTargets
            ),
            parent = if (linkStates) traverserState else traverserState.parent,
            depth = traverserState.depth + 1
        )
    }

    open fun drivePath(traverserState: TraverserState<MS, NS>): TraverserState<MS, NS> =
        drivePath(traverserState, traverserState.nextDirection!!)

    open fun pickDirection(traverserState: TraverserState<MS, NS>, nextNavigatorState: NS): TraverserState<MS, NS> {
        val newMothershipState: MS =
            mothership.pickDirection(traverserState.mothershipState, nextNavigatorState.pickedDirection!!)
        return traverserState.copy(
            mothershipState = newMothershipState,
            navigatorState = navigator.leavingNode(
                nextNavigatorState, newMothershipState.forcedDirection
                    ?: newMothershipState.selectedDirection!!
            ),
            parent = if (linkStates) traverserState else traverserState.parent,
            depth = traverserState.depth + 1
        )
    }

    override val seed: TraverserState<MS, NS> by lazy { TraverserState.getSeed(this) }
    override val value: TraverserState<MS, NS> by lazy { seed }
}

class DefaultTraverser(mothership: Mothership, navigator: Navigator, linkStates: Boolean = true) :
    Traverser<Mothership, MothershipState, Navigator, NavigatorState>(mothership, navigator, linkStates) {
    constructor(planet: Planet, linkStates: Boolean = true) : this(LookupPlanet(planet), linkStates)
    constructor(planet: LookupPlanet, linkStates: Boolean = true) : this(
        Mothership(planet),
        Navigator(planet),
        linkStates
    )
}
