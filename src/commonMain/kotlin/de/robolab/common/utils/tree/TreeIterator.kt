package de.robolab.common.utils.tree

import de.robolab.common.utils.PoolManager

interface ITreeIterable<N> : Iterable<N> {
    override fun iterator(): ITreeIterator<N>
}

interface ITreeIterator<N> : Iterator<N> {
    fun tryAdvance(): N?
}

private data class MappedTreeIterator<N, R>(val source: ITreeIterator<N>, val transform: (N) -> R) : ITreeIterator<R> {
    override fun tryAdvance(): R? = source.tryAdvance()?.let(transform)

    override fun hasNext(): Boolean = source.hasNext()

    override fun next(): R = transform(source.next())
}

fun <N, R> ITreeIterable<N>.mapTree(transform: (N) -> R): ITreeIterable<R> = object : ITreeIterable<R> {
    override fun iterator(): ITreeIterator<R> = MappedTreeIterator(this@mapTree.iterator(), transform)

}

open class TreeIterator<N>(
    val branchFunction: (N) -> List<N>,
    val seed: N,
    lifoPool: Boolean = true
) : ITreeIterator<N>
        where N : Any {

    protected open val manager: PoolManager<N> =
        if (lifoPool) PoolManager.LIFO(seed)
        else PoolManager.FIFO(seed)

    constructor(brancher: ISeededBranchProvider<N>, lifoPool: Boolean = true) : this(
        brancher.branchFunction,
        brancher.seed,
        lifoPool
    )

    constructor(brancher: IBranchProvider<N>, seed: N, lifoPool: Boolean = true) : this(
        brancher.branchFunction,
        seed,
        lifoPool
    )

    override fun hasNext(): Boolean = !manager.isEmpty()

    override fun next(): N = tryAdvance() ?: throw NoSuchElementException()

    override fun tryAdvance(): N? {
        val workingNode: N? = manager.tryRemove()
        if (workingNode != null)
            manager.add(branchFunction(workingNode))
        return workingNode
    }

}

interface ITreeSkiperator<N> : ITreeIterator<N> {
    fun skip()
}

open class TreeSkiperator<N>(branchFunction: (N) -> List<N>, seed: N, lifoPool: Boolean = true) :
    TreeIterator<N>(branchFunction, seed, lifoPool) where N : Any {

    private var current: N? = seed
    private var currentBranches: List<N>? = null

    constructor(brancher: ISeededBranchProvider<N>, lifoPool: Boolean = true) : this(
        brancher.branchFunction,
        brancher.seed,
        lifoPool
    )

    constructor(brancher: IBranchProvider<N>, seed: N, lifoPool: Boolean = true) : this(
        brancher.branchFunction,
        seed,
        lifoPool
    )


    override fun tryAdvance(): N? {
        val localCurrent: N? = current
        if (localCurrent != null) {
            manager.add(currentBranches ?: branchFunction(localCurrent))
        }
        currentBranches = null
        return manager.tryRemove().also { current = it }
    }

    override fun hasNext(): Boolean {
        if (super.hasNext()) return true
        val localCurrent: N = current ?: return false
        val localCurrentBranches: List<N> = currentBranches ?: branchFunction(localCurrent)
        currentBranches = localCurrentBranches
        return localCurrentBranches.isNotEmpty()
    }

    fun skip(): Boolean {
        if (current != null) {
            current = null
            currentBranches = null
            return true
        }
        return false
    }

}