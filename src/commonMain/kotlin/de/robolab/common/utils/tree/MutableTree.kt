package de.robolab.common.utils.tree

import de.robolab.common.utils.PoolManager

interface IMutableBranchProvider<N> {
    val expandFunction: (N) -> Pair<Boolean, List<N>>
}

interface ISeededMutableBranchProvider<N> : IMutableBranchProvider<N> {
    val seedFactory: () -> N
}

class MutableTreeIterator<N>(val branchProvider: ISeededMutableBranchProvider<N>, lifoPool: Boolean = true) :
    Iterator<N> {
    constructor(
        branchProvider: IMutableBranchProvider<N>,
        seedFactory: () -> N,
        lifoPool: Boolean = true
    ) : this(object :
        ISeededMutableBranchProvider<N>,
        IMutableBranchProvider<N> by branchProvider {
        override val seedFactory: () -> N = seedFactory
    }, lifoPool)

    private val manager: PoolManager<N> =
        if (lifoPool) PoolManager.LIFO(branchProvider.seedFactory())
        else PoolManager.FIFO(branchProvider.seedFactory())

    override fun hasNext(): Boolean = !manager.isEmpty()

    override fun next(): N = tryAdvance()!!

    fun tryAdvance(): N? {
        val workingNode: N? = manager.tryRemove()
        if (workingNode != null) {
            val (hasChildren, siblings) = branchProvider.expandFunction(workingNode)
            if (hasChildren)
                manager.add(workingNode)
            manager.add(siblings)
        }
        return workingNode
    }
}

fun <N> ISeededMutableBranchProvider<N>.iterator(): MutableTreeIterator<N> = MutableTreeIterator(this)
