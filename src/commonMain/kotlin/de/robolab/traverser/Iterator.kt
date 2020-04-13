package de.robolab.traverser

open class TreeIterator<N>(val branchFunction: (N) -> List<N>, val seed: N, lifoPool: Boolean = true) : Iterator<N> {

    protected open val manager: PoolManager<N> =
            if (lifoPool) PoolManager.LIFO(seed)
            else PoolManager.FIFO(seed)

    constructor(tree: TreeProvider<N>, lifoPool: Boolean = true) : this(tree.branchFunction, tree.value, lifoPool)
    constructor(brancher: IBranchProvider<N>, seed: N, lifoPool: Boolean = true) : this(brancher::branch, seed, lifoPool)

    override fun hasNext(): Boolean = !manager.isEmpty()

    override fun next(): N = tryAdvance()!!

    fun tryAdvance(): N? {
        val workingNode: N? = manager.tryRemove()
        if (workingNode != null)
            manager.add(branchFunction(workingNode))
        return workingNode
    }


}