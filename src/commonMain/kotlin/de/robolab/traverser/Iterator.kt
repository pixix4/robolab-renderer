package de.robolab.traverser

interface ITreeIterator<N> : Iterator<N>, ISeededBranchProvider<N> {
    fun tryAdvance(): N?
}

open class TreeIterator<N>(final override val branchFunction: (N) -> List<N>, final override val seed: N, lifoPool: Boolean = true): ITreeIterator<N> {

    protected open val manager: PoolManager<N> =
            if (lifoPool) PoolManager.LIFO(seed)
            else PoolManager.FIFO(seed)

    constructor(brancher: ISeededBranchProvider<N>, lifoPool: Boolean = true) : this(brancher.branchFunction, brancher.seed, lifoPool)
    constructor(brancher: IBranchProvider<N>, seed: N, lifoPool: Boolean = true) : this(brancher.branchFunction, seed, lifoPool)

    override fun hasNext(): Boolean = !manager.isEmpty()

    override fun next(): N = tryAdvance()!!

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

open class TreeSkiperator<N>(branchFunction : (N)->List<N>, seed: N, lifoPool: Boolean = true) : TreeIterator<N>(branchFunction, seed, lifoPool){

    private var current: N? = seed
    private var currentBranches: List<N>? = null

    constructor(brancher: ISeededBranchProvider<N>, lifoPool: Boolean = true) : this(brancher.branchFunction, brancher.seed, lifoPool)
    constructor(brancher: IBranchProvider<N>, seed: N, lifoPool: Boolean = true) : this(brancher.branchFunction, seed, lifoPool)


    override fun tryAdvance(): N? {
        val localCurrent: N? = current
        if(localCurrent != null){
            manager.add(currentBranches?:branchFunction(localCurrent))
        }
        currentBranches = null
        return manager.tryRemove().also { current = it }
    }

    override fun hasNext(): Boolean {
        if(super.hasNext()) return true
        val localCurrent: N = current ?: return false
        val localCurrentBranches : List<N> = currentBranches ?: branchFunction(localCurrent)
        currentBranches = localCurrentBranches
        return localCurrentBranches.isNotEmpty()
    }

    fun skip() : Boolean{
        if(current != null){
            current = null
            currentBranches = null
            return true
        }
        return false
    }

}