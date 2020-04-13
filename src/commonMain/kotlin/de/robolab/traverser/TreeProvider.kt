package de.robolab.traverser

interface IBranchProvider<N> {
    fun branch(node: N): List<N>
}

interface ITreeProvider<out N> : Iterable<N> {
    fun children(): List<N>
    fun branch(): List<ITreeProvider<N>>
    val value: N
}

data class TreeProvider<N>(val branchFunction: (N) -> List<N>, override val value: N) : ITreeProvider<N> {
    fun children(node: N): List<N> = branchFunction(node)
    override fun children(): List<N> = children(value)
    fun branch(node: N): List<TreeProvider<N>> = children(node).map { TreeProvider(branchFunction, it) }
    override fun branch(): List<TreeProvider<N>> = branch(value)

    constructor(brancher: IBranchProvider<N>, value: N) : this(brancher::branch, value)

    override fun iterator(): Iterator<N> = TreeIterator(this)
}