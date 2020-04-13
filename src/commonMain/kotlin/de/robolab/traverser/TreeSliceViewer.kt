package de.robolab.traverser

import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.ObservableList
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.*

fun <N> ISeededBranchProvider<N>.treeSliceViewer(): TreeSliceViewer<N> = ObservableTreeSliceViewer(this)

fun <N> ISeededBranchProvider<N>.observableTreeSliceViewer(): ObservableTreeSliceViewer<N> = ObservableTreeSliceViewer(this)

fun <T> property(
        readonlyAccessorFunction: () -> T,
        vararg properties: ReadOnlyProperty<*>
): ReadOnlyProperty<T> = FunctionReadOnlyProperty(
        object : FunctionReadOnlyAccessor<T> {
            override fun get(): T = readonlyAccessorFunction()
        },
        *properties)

open class TreeSliceViewer<N> protected constructor(
        override val branchFunction: (N) -> List<N>,
        private val _entries: MutableList<TreeSliceEntry<N>>,
        final override val seed: N = _entries.first().currentOption) : ISeededBranchProvider<N>, List<TreeSliceViewer.TreeSliceEntry<N>> by _entries {
    constructor(branchFunction: (N) -> List<N>, seed: N) : this(branchFunction, mutableListOf(TreeSliceEntry<N>(seed)), seed)
    constructor(brancher: IBranchProvider<N>, seed: N) : this(brancher.branchFunction, seed)
    constructor(brancher: ISeededBranchProvider<N>) : this(brancher.branchFunction, brancher.seed)

    init {
        if (_entries.isEmpty())
            _entries.add(TreeSliceEntry(seed))
    }

    val entries: List<TreeSliceEntry<N>> by lazy { _entries }

    val currentNode: N
        get() = _entries.last().currentOption

    val currentNodes: List<N>
        get() = _entries.map(TreeSliceEntry<N>::currentOption)


    fun expand(): Boolean {
        val newAlternatives: List<N> = branchFunction(currentNode)
        if (newAlternatives.isEmpty()) return false
        _entries.add(TreeSliceEntry(0, newAlternatives))
        return true
    }

    fun nextAlternative(depth: Int): Boolean {
        if (depth !in _entries.indices) throw IndexOutOfBoundsException()
        val entry: TreeSliceEntry<N> = _entries[depth]
        if (!entry.hasNext) return false
        _entries.subList(depth + 1, _entries.size).clear()
        _entries[depth] = entry.next()
        return true
    }

    fun nextAlternative(predicate: (TreeSliceEntry<N>) -> Boolean): Boolean = _entries.indexOfLast(predicate).let { if (it in _entries.indices) nextAlternative(it) else false }

    fun nextAlternative(node: N): Boolean = nextAlternative { node == it.currentOption }

    fun next(): Boolean = nextAlternative(TreeSliceEntry<N>::hasNext)

    fun hasNext(): Boolean = entries.any(TreeSliceEntry<N>::hasNext)

    fun previousAlternative(depth: Int): Boolean {
        if (depth !in _entries.indices) throw IndexOutOfBoundsException()
        val entry: TreeSliceEntry<N> = _entries[depth]
        if (!entry.hasPrevious) return false
        _entries.subList(depth + 1, _entries.size).clear()
        _entries[depth] = entry.previous()
        return true
    }

    fun previousAlternative(predicate: (TreeSliceEntry<N>) -> Boolean): Boolean = _entries.indexOfLast(predicate).let { if (it in _entries.indices) previousAlternative(it) else false }

    fun previousAlternative(node: N): Boolean = previousAlternative { node == it.currentOption }

    fun previous(): Boolean = previousAlternative(TreeSliceEntry<N>::hasPrevious)

    fun hasPrevious(): Boolean = entries.any(TreeSliceEntry<N>::hasPrevious)

    @Suppress("ControlFlowWithEmptyBody")
    fun fullExpand(): Boolean {
        if (!expand()) return false
        while (expand());
        return true
    }

    fun fullExpand(predicate: (N) -> Boolean): Boolean {
        if (!predicate(currentNode)) return false
        if (!expand()) return false
        while (predicate(currentNode))
            if (!expand())
                break
        return true
    }

    fun fullExpandNext(): Boolean = if (next()) fullExpand().let { true } else false
    fun fullExpandNextAlternative(depth: Int): Boolean = if (nextAlternative(depth)) fullExpand().let { true } else false
    fun fullExpandNextAlternative(node: N): Boolean = if (nextAlternative(node)) fullExpand().let { true } else false
    fun fullExpandNextAlternative(predicate: (TreeSliceEntry<N>) -> Boolean): Boolean = if (nextAlternative(predicate)) fullExpand().let { true } else false
    fun fullExpandPrevious(): Boolean = if (previous()) fullExpand().let { true } else false
    fun fullExpandPreviousAlternative(depth: Int): Boolean = if (previousAlternative(depth)) fullExpand().let { true } else false
    fun fullExpandPreviousAlternative(node: N): Boolean = if (previousAlternative(node)) fullExpand().let { true } else false
    fun fullExpandPreviousAlternative(predicate: (TreeSliceEntry<N>) -> Boolean): Boolean = if (previousAlternative(predicate)) fullExpand().let { true } else false

    data class TreeSliceEntry<N>(val currentIndex: Int, val options: List<N>) {
        constructor(options: List<N>) : this(0, options)
        constructor(element: N) : this(0, listOf(element))

        val currentOption: N = options[currentIndex]

        val hasNext: Boolean = currentIndex < options.size
        val hasPrevious: Boolean = currentIndex > 0

        fun next(): TreeSliceEntry<N> = if (hasNext) TreeSliceEntry(currentIndex + 1, options) else this
        fun previous(): TreeSliceEntry<N> = if (hasPrevious) TreeSliceEntry(currentIndex - 1, options) else this
    }
}

class ObservableTreeSliceViewer<N> private constructor(
        branchFunction: (N) -> List<N>,
        seed: N,
        private val _observableEntries: ObservableList<TreeSliceEntry<N>>) :
        TreeSliceViewer<N>(branchFunction, _observableEntries, seed),
        ObservableReadOnlyList<TreeSliceViewer.TreeSliceEntry<N>> by _observableEntries {
    override fun subList(fromIndex: Int, toIndex: Int): ObservableReadOnlyList<TreeSliceEntry<N>> = _observableEntries.subList(fromIndex, toIndex)
    override fun iterator(): Iterator<TreeSliceEntry<N>> = _observableEntries.iterator()
    override fun listIterator(): ListIterator<TreeSliceEntry<N>> = observableEntries.listIterator()
    override fun listIterator(index: Int): ListIterator<TreeSliceEntry<N>> = observableEntries.listIterator(index)
    override fun contains(element: TreeSliceEntry<N>): Boolean = _observableEntries.contains(element)
    override fun containsAll(elements: Collection<TreeSliceEntry<N>>): Boolean = _observableEntries.containsAll(elements)
    override fun get(index: Int): TreeSliceEntry<N> = _observableEntries[index]
    override fun indexOf(element: TreeSliceEntry<N>): Int = _observableEntries.indexOf(element)
    override fun isEmpty(): Boolean = _observableEntries.isEmpty()
    override fun lastIndexOf(element: TreeSliceEntry<N>): Int = _observableEntries.lastIndexOf(element)
    override val size: Int
        get() = _observableEntries.size

    constructor(brancher: IBranchProvider<N>, seed: N) : this(brancher.branchFunction, seed)
    constructor(brancher: ISeededBranchProvider<N>) : this(brancher.branchFunction, brancher.seed)
    constructor(branchFunction: (N) -> List<N>, seed: N) : this(branchFunction, seed, observableListOf(TreeSliceEntry(seed)))

    val observableEntries: ObservableReadOnlyList<TreeSliceEntry<N>> = _observableEntries

    val hasNextProperty: ReadOnlyProperty<Boolean> = property(this::hasNext, observableEntries)
    val hasPreviousProperty: ReadOnlyProperty<Boolean> = property(this::hasPrevious, observableEntries)
    val currentNodeProperty: ReadOnlyProperty<N> = property(this::currentNode, observableEntries)
}