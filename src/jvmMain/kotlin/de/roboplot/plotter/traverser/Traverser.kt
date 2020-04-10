package de.roboplot.plotter.traverser

import de.roboplot.plotter.model.*
import java.lang.IllegalArgumentException

open class Traverser<M, MS, N, NS>(val mothership: M, val navigator: N, val linkStates: Boolean = false) : Iterable<TraverserState<MS, NS>>
        where M : IMothership<MS>, MS : IMothershipState {

    val planet: LookupPlanet = mothership.planet

    open fun getNextDirections(traverserState: TraverserState<MS, NS>): Sequence<Direction?> = with(traverserState) {
        //  if nextDirection null, explorationComplete/targetReached
        //  if multiple directions, try to reduce to forcedDir if present and element
        val forcedDir: Direction? = mothership.peekForceDirection(mothershipState)
        return@with sequenceOf(forcedDir)
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

        val nextDirections: Sequence<Direction?> = getNextDirections(traverserState)

        return@with nextDirections.map {
            if (it == null)
                return@map copy(status = TraverserState.Status.ExplorationComplete)
            else
                return@map drivePath(pickDirection(traverserState, it))
        }
    }

    fun drivePath(traverserState: TraverserState<MS, NS>, direction: Direction): TraverserState<MS, NS> =
            drivePath(traverserState, planet.getPath(traverserState.location, direction)!!)

    fun drivePath(traverserState: TraverserState<MS, NS>, path: Path): TraverserState<MS, NS> = traverserState.copy(
            mothershipState = mothership.drivePath(traverserState.mothershipState, path),
            parent = if (linkStates) traverserState else traverserState.parent
    )

    fun drivePath(traverserState: TraverserState<MS, NS>): TraverserState<MS, NS> = drivePath(traverserState, traverserState.nextDirection!!)

    fun pickDirection(traverserState: TraverserState<MS, NS>, direction: Direction): TraverserState<MS, NS> = traverserState.copy(
            mothershipState = mothership.pickDirection(traverserState.mothershipState, direction),
            parent = if (linkStates) traverserState else traverserState.parent
    )

    override fun iterator(): TraverserIterator<M, MS, N, NS> = TraverserIterator(this)

}

class DefaultTraverser(mothership: Mothership, navigator: Mothership, linkStates: Boolean = false) :
        Traverser<Mothership, MothershipState, Mothership, MothershipState>(mothership, navigator, linkStates) {
    constructor(planet: Planet, linkStates: Boolean = false) : this(LookupPlanet(planet), linkStates)
    constructor(planet: LookupPlanet, linkStates: Boolean = false) : this(Mothership(planet), Mothership(planet), linkStates)
}
