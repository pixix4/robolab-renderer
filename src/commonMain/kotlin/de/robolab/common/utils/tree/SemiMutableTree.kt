package de.robolab.common.utils.tree

import de.robolab.common.utils.PoolManager

abstract class SemiMutableTreeProvider<I, M> : ISeededBranchProvider<I>, ISeededMutableBranchProvider<M>, Iterable<I>
        where M : Any {

    abstract override val seed: I
    override val seedFactory: () -> M = { createMutable(seed) }
    abstract fun createMutable(immutable: I): M
    abstract fun extractImmutable(mutable: M): I

    override fun iterator(): SemiMutableTreeIterator<I, M> = SemiMutableTreeIterator(this)

    override val branchFunction: (I) -> List<I> = ::branch
    override val expandFunction: (M) -> Pair<Boolean, List<M>> = {
        val siblings = expand(it)
        canExpand(it) to siblings
    }

    abstract fun canExpand(immutable: I): Boolean
    open fun canExpand(mutable: M): Boolean = canExpand(extractImmutable(mutable))

    abstract fun expand(mutable: M): List<M>

    open fun branch(immutable: I): List<I> {
        if (!canExpand(immutable)) return emptyList()
        return unsafeMutableChildren(createMutable(immutable)).map(::extractImmutable)
    }

    fun mutableChildren(mutable: M): List<M> =
        if (canExpand(mutable)) unsafeMutableChildren(mutable) else emptyList()

    fun unsafeMutableChildren(mutable: M): List<M> = listOf(mutable) + expand(mutable)
}

class SemiMutableTreeIterator<I, M>(
    val provider: SemiMutableTreeProvider<I, M>,
    override val seed: I = provider.seed,
    lifoPool: Boolean = true
) : ITreeIterator<I> where M : Any {
    private val manager: PoolManager<M> = PoolManager.create(provider.createMutable(seed), lifo = lifoPool)

    override val branchFunction: (I) -> List<I> = provider.branchFunction

    override fun hasNext(): Boolean = !manager.isEmpty()

    override fun next(): I = tryAdvance() ?: throw NoSuchElementException()

    override fun tryAdvance(): I? {
        val workingNode: M = manager.tryRemove() ?: return null
        val immutable = provider.extractImmutable(workingNode)
        if (provider.canExpand(immutable))
            manager.add(provider.unsafeMutableChildren(workingNode))
        return immutable
    }
}
