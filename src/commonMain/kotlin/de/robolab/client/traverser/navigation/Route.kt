package de.robolab.client.traverser.navigation

import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.PlanetDirection

data class Route(
    val steps: List<StartStep>,
    val end: PlanetPoint,
) {
    val start: PlanetPoint = steps.firstOrNull()?.start ?: end

    val length = steps.size

    val cost: Float by lazy { steps.map(StartStep::cost).sum() }

    val locations: List<PlanetPoint> by lazy { steps.map(StartStep::start) + listOf(end) }

    val firstStep: StartStep? = steps.firstOrNull()

    operator fun get(index: Int): PlanetPoint = when {
        index < steps.size -> steps[index].start
        index == steps.size -> end
        else -> locations[index]
    }

    fun isEmpty() = steps.isEmpty()

    fun hasCycles(): Boolean {
        val seenCoordinates: MutableSet<PlanetPoint> = mutableSetOf(end)
        for (step in steps)
            if (seenCoordinates.add(step.start))
                return true
        return false
    }

    fun startsWith(other: Route, onlyDirections: Boolean = false): Boolean {
        if (other.length > length) return false
        val ownList: List<StartStep> = steps.subList(0, other.length)
        return (onlyDirections || other.end == this[other.length]) && ownList.equals(other.steps, onlyDirections)
    }

    fun endsWith(other: Route, onlyDirections: Boolean = false): Boolean {
        if (other.length > length) return false
        val ownList: List<StartStep> = steps.subList(length - other.length, length)
        return (onlyDirections || other.end == end) && ownList.equals(other.steps, onlyDirections)
    }

    fun equals(other: Route, onlyDirections: Boolean = false): Boolean {
        return (onlyDirections || other.end == end) && steps.equals(other.steps, onlyDirections)
    }

    fun subRoute(startStepIndex: Int, endStepIndex: Int): Route =
        Route(steps.subList(startStepIndex, endStepIndex), this[endStepIndex])

    fun getFirst(stepCount: Int): Route = subRoute(0, stepCount)
    fun getLast(stepCount: Int): Route = subRoute(length - stepCount, length)

    operator fun plus(step: EndStep): Route {
        val (startStep, newEnd) = step.toStartStep(end)
        return Route(steps + listOf(startStep), newEnd)
    }

    operator fun plus(other: Route) = chain(other, false)

    fun chain(following: Route, translate: Boolean = false): Route = when {
        following.start == end -> Route(steps + following.steps, following.end)
        translate -> {
            val delta = PlanetPoint(end.x - following.start.x, end.y - following.start.y)
            Route(
                steps + following.steps.map { StartStep(it.start.translate(delta), it.direction) },
                following.end.translate(delta)
            )
        }
        else -> throw IllegalArgumentException("End and start mismatch of routes to be chained: $end != ${following.start}")
    }

    companion object {
        fun empty(location: PlanetPoint) = Route(emptyList(), location)
        fun build(start: PlanetPoint, block: Builder.() -> Unit): Route = Builder(start).apply(block).build()

        fun List<StartStep>.toEndSteps(end: PlanetPoint): Pair<PlanetPoint, List<EndStep>> {
            return if (isEmpty()) end to emptyList()
            else first().start to (this + listOf(StartStep(end, PlanetDirection.North))).chunked(2) { (current, next) ->
                EndStep(current.direction, next.start, current.cost)
            }
        }

        fun List<EndStep>.toStartSteps(start: PlanetPoint): Pair<List<StartStep>, PlanetPoint> {
            return if (isEmpty()) emptyList<StartStep>() to start
            else (listOf(EndStep(PlanetDirection.North, start)) + this).chunked(2) { (previous, current) ->
                StartStep(previous.end, current.direction, current.cost)
            } to last().end
        }

        fun StartStep.toEndStep(end: PlanetPoint): Pair<PlanetPoint, EndStep> = start to EndStep(direction, end, cost)
        fun EndStep.toStartStep(start: PlanetPoint): Pair<StartStep, PlanetPoint> =
            StartStep(start, direction, cost) to end

        fun List<StartStep>.equals(other: List<StartStep>, onlyDirections: Boolean = false): Boolean {
            return if (onlyDirections) this.map(StartStep::direction) == other.map(StartStep::direction)
            else this == other
        }
    }

    data class StartStep(
        val start: PlanetPoint,
        val direction: PlanetDirection,
        val cost: Float = 1f,
    ) {
        override fun toString(): String = "(${start.x},${start.y},${direction.letter}:$cost)"
    }

    data class EndStep(
        val direction: PlanetDirection,
        val end: PlanetPoint,
        val cost: Float = 1f,
    ) {
        override fun toString(): String = "({${direction.letter}},${end.x},${end.y}: $cost)"
    }

    class Builder private constructor(
        private var _end: PlanetPoint,
        private val _steps: MutableList<StartStep>,
    ) {
        constructor(start: PlanetPoint) : this(start, mutableListOf())

        val start: PlanetPoint
            get() = _steps.firstOrNull()?.start ?: _end
        val end: PlanetPoint
            get() = _end
        val steps: List<StartStep> = _steps

        fun build(): Route = Route(_steps.toList(), _end)

        fun clone(): Builder = Builder(_end, _steps.toMutableList())

        fun append(direction: PlanetDirection, end: PlanetPoint, cost: Float = 1f) {
            _steps.add(StartStep(end, direction, cost))
            _end = end
        }

        fun append(step: EndStep) = append(step.direction, step.end, step.cost)

        fun prepend(start: PlanetPoint, direction: PlanetDirection, cost: Float = 1f) =
            prepend(StartStep(start, direction, cost))

        fun prepend(step: StartStep) {
            _steps.add(0, step)
        }

        fun appendAll(steps: List<EndStep>) {
            val (startSteps, newEnd) = steps.toStartSteps(_end)
            _steps.addAll(startSteps)
            _end = newEnd
        }

        fun prependAll(steps: List<StartStep>) {
            _steps.addAll(0, steps)
        }

        fun removeFirst(count: Int): List<StartStep> {
            val sublist = _steps.subList(0, count)
            val removed = sublist.toList()
            sublist.clear()
            return removed
        }

        fun removeLast(count: Int): List<EndStep> {
            if (count == 0) return emptyList()
            val sublist = _steps.subList(_steps.size - count, _steps.size)
            val (newEnd, removed) = sublist.toEndSteps(_end)
            _end = newEnd
            return removed
        }
    }
}
