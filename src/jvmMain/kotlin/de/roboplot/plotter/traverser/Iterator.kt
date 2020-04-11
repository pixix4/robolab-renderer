package de.roboplot.plotter.traverser

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
            private val pool: MutableList<T> = mutableListOf(*element)

            override fun add(element: T) {
                pool.add(element)
            }

            override fun add(elements: Collection<T>) {
                pool.addAll(elements)
            }

            override fun add(vararg elements: T) {
                pool.addAll(elements)
            }

            override fun add(elements: Sequence<T>) {
                pool.addAll(elements)
            }

            override fun tryRemove(): T? = synchronized(pool) {
                if (pool.isEmpty())
                    null
                else
                    pool.removeAt(0)
            }

            override fun isEmpty(): Boolean = pool.isEmpty()

        }

        class LIFO<T>(vararg element: T) : PoolManager<T> {
            private val pool: MutableList<T> = mutableListOf(*element)

            override fun add(element: T) {
                pool.add(element)
            }

            override fun add(elements: Collection<T>) {
                pool.addAll(elements)
            }

            override fun add(vararg elements: T) {
                pool.addAll(elements)
            }

            override fun add(elements: Sequence<T>) {
                pool.addAll(elements)
            }

            override fun tryRemove(): T? = synchronized(pool) {
                if (pool.isEmpty())
                    null
                else
                    pool.removeAt(pool.lastIndex)
            }

            override fun isEmpty(): Boolean = pool.isEmpty()
        }
    }
}