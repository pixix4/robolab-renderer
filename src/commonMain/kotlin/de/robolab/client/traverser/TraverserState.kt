package de.robolab.client.traverser

import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path

interface ITraverserState<out TS> where TS : ITraverserState<TS> {
    val mothershipState: IMothershipState
    val navigatorState: INavigatorState
    val nextDirection: Direction?
    val location: Coordinate
    val parent: TS?
    val beforePoint: Boolean
        get() = mothershipState.beforePoint
    val lastDrivenPath: Path
        get() = mothershipState.drivenPath

    fun traceUp(): Sequence<ITraverserState<TS>> = generateSequence(this, ITraverserState<TS>::parent)
    fun getTrail(): ITraverserTrail = TraverserTrail(traceUp()
        .drop(1)
        .filter { (!it.mothershipState.beforePoint) || !it.running }
        .map { Pair(it.location, it.nextDirection) }
        .toList()
        .asReversed()
            + Pair(location, nextDirection), mothershipState, navigatorState, status, statusInfo)

    val depth: Int
    val status: Status
    val statusInfo: Any?
    val running: Boolean
        get() = status == Status.Running

    enum class Status {
        Running,
        TargetReached,
        ExplorationComplete,
        TraverserCrashed,
        RobotCrashed
    }
}

data class TraverserState<MS, NS>(
    override val mothershipState: MS,
    override val navigatorState: NS,
    override val status: ITraverserState.Status,
    override val statusInfo: Any? = null,
    override val parent: TraverserState<MS, NS>?,
    override val depth: Int = (parent?.depth ?: -1) + 1
) : ITraverserState<TraverserState<MS, NS>>
        where MS : IMothershipState, NS : INavigatorState {
    override val nextDirection: Direction?
        get() = mothershipState.forcedDirection.let {
            if (it == null) return@let mothershipState.selectedDirection
            else return@let it
        }

    override val location: Coordinate = mothershipState.currentLocation

    companion object Seed {
        fun <M, MS, N, NS> getSeed(traverser: Traverser<M, MS, N, NS>): TraverserState<MS, NS>
                where M : IMothership<MS>, MS : IMothershipState, N : INavigator<NS>, NS : INavigatorState =
            TraverserState(
                mothershipState = traverser.mothership.seedState,
                navigatorState = traverser.navigator.seedState,
                parent = null,
                status = ITraverserState.Status.Running,
                statusInfo = null
            )
    }
}
