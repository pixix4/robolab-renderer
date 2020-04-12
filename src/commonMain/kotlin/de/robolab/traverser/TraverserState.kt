package de.robolab.traverser

import de.robolab.planet.Coordinate
import de.robolab.planet.Direction

interface ITraverserState<out TS> where TS : ITraverserState<TS> {
    val nextDirection: Direction?
    val location: Coordinate
    val parent: TS?
    fun getTrail(): ITraverserTrail
}

data class TraverserState<MS, NS>(val mothershipState: MS,
                                  val navigatorState: NS,
                                  val status: Status,
                                  val statusCause: Any? = null,
                                  override val parent: TraverserState<MS, NS>?) : ITraverserState<TraverserState<MS, NS>>
        where MS : IMothershipState {
    override val nextDirection: Direction?
        get() = mothershipState.forcedDirection.let {
            if (it == null) return@let mothershipState.selectedDirection
            else return@let it
        }

    override val location: Coordinate = mothershipState.currentLocation

    enum class Status {
        Running,
        TargetReached,
        ExplorationComplete,
        TraverserCrashed,
        RobotCrashed
    }

    companion object Seed {
        fun <M, MS, N, NS> getSeed(traverser: Traverser<M, MS, N, NS>): TraverserState<MS, NS>
                where M : IMothership<MS>, MS : IMothershipState, N : INavigator<NS>, NS : INavigatorState = TraverserState(
                mothershipState = traverser.mothership.seedState,
                navigatorState = traverser.navigator.seedState,
                parent = null,
                status = Status.Running,
                statusCause = null
        )
    }

    override fun getTrail(): ITraverserTrail =
            TraverserTrail(traceUp()
                    .drop(1)
                    .filter { !it.mothershipState.beforePoint }
                    .map { Pair(it.location, it.nextDirection) }
                    .toList()
                    .asReversed()
                    + Pair(location, nextDirection), status, statusCause)

    protected fun traceUp(): Sequence<TraverserState<MS, NS>> = generateSequence(this, TraverserState<MS, NS>::parent)
}
