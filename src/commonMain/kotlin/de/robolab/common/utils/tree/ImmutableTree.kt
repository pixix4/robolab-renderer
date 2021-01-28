package de.robolab.common.utils.tree

interface IBranchProvider<N> {
    val branchFunction: (N) -> List<N>
}

interface ISeededBranchProvider<N> : IBranchProvider<N> {
    val seed: N
}

interface ITreeProvider<out N> : Iterable<N> {
    fun children(): List<N>
    fun branch(): List<ITreeProvider<N>>
    val value: N
}

data class TreeProvider<N>(override val branchFunction: (N) -> List<N>, override val value: N) : ITreeProvider<N>,
    ISeededBranchProvider<N> where N : Any {
    override val seed: N = value
    override fun children(): List<N> = branchFunction(value)
    fun branchTree(node: N): List<TreeProvider<N>> = branchFunction(node).map { TreeProvider(branchFunction, it) }
    override fun branch(): List<TreeProvider<N>> = branchTree(value)

    constructor(brancher: IBranchProvider<N>, value: N) : this(brancher.branchFunction, value)

    override fun iterator(): Iterator<N> = TreeIterator(this)
}