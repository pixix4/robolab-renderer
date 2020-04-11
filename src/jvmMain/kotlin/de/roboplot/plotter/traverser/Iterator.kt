package de.roboplot.plotter.traverser

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue

open class TraverserIterator<M, MS, N, NS>(val parent: Traverser<M, MS, N, NS>, lifoPool: Boolean = true) : Iterator<TraverserState<MS, NS>>
        where M : IMothership<MS>, MS : IMothershipState, N : INavigator<NS>, NS : INavigatorState {

    protected open val manager: PoolManager<TraverserState<MS, NS>> =
            if (lifoPool) PoolManager.LIFO(TraverserState.getSeed(parent))
            else PoolManager.FIFO(TraverserState.getSeed(parent))

    override fun hasNext(): Boolean = !manager.isEmpty()

    override fun next(): TraverserState<MS, NS> = tryAdvance()!!

    fun tryAdvance(): TraverserState<MS, NS>? {
        val workingState: TraverserState<MS, NS>? = manager.tryRemove()
        if (workingState != null && workingState.status == TraverserState.Status.Running)
            manager.add(parent.branch(workingState))
        return workingState
    }


    protected interface PoolManager<T> {
        fun add(element: T)
        fun add(elements: Collection<T>) = elements.forEach(this::add)
        fun add(vararg elements: T) = elements.forEach(this::add)
        fun add(elements: Sequence<T>) = elements.forEach(this::add)

        fun tryRemove(): T?

        fun isEmpty(): Boolean

        class FIFO<T>(vararg element: T) : PoolManager<T> {
            private val queue: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue<T>()

            init {
                queue.addAll(element)
            }

            override fun add(element: T) {
                queue.add(element)
            }

            override fun add(elements: Collection<T>) {
                queue.addAll(elements)
            }

            override fun add(vararg elements: T) {
                queue.addAll(elements)
            }

            override fun add(elements: Sequence<T>) {
                queue.addAll(elements)
            }

            override fun tryRemove(): T? = synchronized(queue) {
                if (queue.isEmpty())
                    null
                else
                    queue.remove()
            }

            override fun isEmpty(): Boolean = queue.isEmpty()

        }

        class LIFO<T>(vararg element: T) : PoolManager<T> {
            private val stack: ConcurrentLinkedDeque<T> = ConcurrentLinkedDeque<T>()

            init {
                stack.addAll(element)
            }

            override fun add(element: T) {
                stack.add(element)
            }

            override fun add(elements: Collection<T>) {
                stack.addAll(elements)
            }

            override fun add(vararg elements: T) {
                stack.addAll(elements)
            }

            override fun add(elements: Sequence<T>) {
                stack.addAll(elements)
            }

            override fun tryRemove(): T? = synchronized(stack) {
                if (stack.isEmpty())
                    null
                else
                    stack.removeLast()
            }

            override fun isEmpty(): Boolean = stack.isEmpty()
        }
    }
}