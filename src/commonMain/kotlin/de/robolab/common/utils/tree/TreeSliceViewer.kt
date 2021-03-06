package de.robolab.common.utils.tree

import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.property
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun <N> ISeededBranchProvider<N>.treeSliceViewer(): TreeSliceViewer<N> = TreeSliceViewer(this)

fun <N> ISeededBranchProvider<N>.observableTreeSliceViewer(): ObservableTreeSliceViewer<N> =
    ObservableTreeSliceViewer(this)

open class TreeSliceViewer<N> protected constructor(
    override val branchFunction: (N) -> List<N>,
    private val _entries: MutableList<TreeSliceEntry<N>>,
    final override val seed: N = _entries.first().currentOption
) : ISeededBranchProvider<N>, List<TreeSliceViewer.TreeSliceEntry<N>> by _entries {
    constructor(branchFunction: (N) -> List<N>, seed: N) : this(
        branchFunction,
        mutableListOf(TreeSliceEntry<N>(seed)),
        seed
    )

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


    fun expand(isLeftExpand: Boolean = true): Boolean {
        val newAlternatives: List<N> = branchFunction(currentNode)
        if (newAlternatives.isEmpty()) return false
        _entries.add(TreeSliceEntry(if (isLeftExpand) 0 else newAlternatives.lastIndex, newAlternatives))
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

    fun nextAlternative(predicate: (TreeSliceEntry<N>) -> Boolean): Boolean =
        _entries.indexOfLast(predicate).let { if (it in _entries.indices) nextAlternative(it) else false }

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

    fun previousAlternative(predicate: (TreeSliceEntry<N>) -> Boolean): Boolean =
        _entries.indexOfLast(predicate).let { if (it in _entries.indices) previousAlternative(it) else false }

    fun previousAlternative(node: N): Boolean = previousAlternative { node == it.currentOption }

    fun previous(): Boolean = previousAlternative(TreeSliceEntry<N>::hasPrevious)

    fun hasPrevious(): Boolean = entries.any(TreeSliceEntry<N>::hasPrevious)

    @Suppress("ControlFlowWithEmptyBody")
    fun fullExpand(isLeftExpand: Boolean = true): Boolean {
        if (!expand(isLeftExpand)) return false
        while (expand(isLeftExpand));
        return true
    }

    @Suppress("ControlFlowWithEmptyBody")
    suspend fun fullExpandAsync(
        isLeftExpand: Boolean = true,
        delay: Duration = PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
    ): Boolean {
        if (!expand(isLeftExpand)) return false
        if (delay == Duration.ZERO) yield()
        else delay(delay)
        while (expand(isLeftExpand))
            if (delay == Duration.ZERO) yield()
            else delay(delay)

        return true
    }

    fun fullExpand(predicate: (N) -> Boolean, isLeftExpand: Boolean = true): Boolean {
        if (!predicate(currentNode)) return false
        if (!expand(isLeftExpand)) return false
        while (predicate(currentNode))
            if (!expand(isLeftExpand))
                break
        return true
    }

    suspend fun fullExpandAsync(
        predicate: (N) -> Boolean,
        isLeftExpand: Boolean = true,
        delay: Duration = PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
    ): Boolean {
        if (!predicate(currentNode)) return false
        if (!expand(isLeftExpand)) return false
        if (delay == Duration.ZERO) yield()
        else delay(delay)

        while (predicate(currentNode))
            if (!expand(isLeftExpand))
                break
            else {
                if (delay == Duration.ZERO) yield()
                else delay(delay)
            }
        return true
    }

    fun fullExpandNext(isLeftExpand: Boolean = true): Boolean =
        if (next()) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandNextAsync(isLeftExpand: Boolean = true): Boolean =
        if (next()) fullExpandAsync(isLeftExpand).let { true } else false

    fun fullExpandNextAlternative(depth: Int, isLeftExpand: Boolean = true): Boolean =
        if (nextAlternative(depth)) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandNextAlternativeAsync(depth: Int, isLeftExpand: Boolean = true): Boolean =
        if (nextAlternative(depth)) fullExpandAsync(isLeftExpand).let { true } else false

    fun fullExpandNextAlternative(node: N, isLeftExpand: Boolean = true): Boolean =
        if (nextAlternative(node)) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandNextAlternativeAsync(node: N, isLeftExpand: Boolean = true): Boolean =
        if (nextAlternative(node)) fullExpandAsync(isLeftExpand).let { true } else false

    fun fullExpandNextAlternative(isLeftExpand: Boolean = true, predicate: (TreeSliceEntry<N>) -> Boolean): Boolean =
        if (nextAlternative(predicate)) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandNextAlternativeAsync(
        isLeftExpand: Boolean = true,
        predicate: (TreeSliceEntry<N>) -> Boolean
    ): Boolean =
        if (nextAlternative(predicate)) fullExpandAsync(isLeftExpand).let { true } else false

    fun fullExpandPrevious(isLeftExpand: Boolean = false): Boolean =
        if (previous()) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandPreviousAsync(isLeftExpand: Boolean = false): Boolean =
        if (previous()) fullExpandAsync(isLeftExpand).let { true } else false

    fun fullExpandPreviousAlternative(depth: Int, isLeftExpand: Boolean = false): Boolean =
        if (previousAlternative(depth)) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandPreviousAlternativeAsync(depth: Int, isLeftExpand: Boolean = false): Boolean =
        if (previousAlternative(depth)) fullExpandAsync(isLeftExpand).let { true } else false

    fun fullExpandPreviousAlternative(node: N, isLeftExpand: Boolean = false): Boolean =
        if (previousAlternative(node)) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandPreviousAlternativeAsync(node: N, isLeftExpand: Boolean = false): Boolean =
        if (previousAlternative(node)) fullExpandAsync(isLeftExpand).let { true } else false

    fun fullExpandPreviousAlternative(
        isLeftExpand: Boolean = false,
        predicate: (TreeSliceEntry<N>) -> Boolean
    ): Boolean =
        if (previousAlternative(predicate)) fullExpand(isLeftExpand).let { true } else false

    suspend fun fullExpandPreviousAlternativeAsync(
        isLeftExpand: Boolean = false,
        predicate: (TreeSliceEntry<N>) -> Boolean
    ): Boolean =
        if (previousAlternative(predicate)) fullExpandAsync(isLeftExpand).let { true } else false

    data class TreeSliceEntry<N>(val currentIndex: Int, val options: List<N>) {
        constructor(options: List<N>) : this(0, options)
        constructor(element: N) : this(0, listOf(element))

        val currentOption: N = options[currentIndex]

        val hasNext: Boolean = currentIndex + 1 < options.size
        val hasPrevious: Boolean = currentIndex > 0

        fun next(): TreeSliceEntry<N> = if (hasNext) TreeSliceEntry(currentIndex + 1, options) else this
        fun previous(): TreeSliceEntry<N> = if (hasPrevious) TreeSliceEntry(currentIndex - 1, options) else this
    }
}

class ObservableTreeSliceViewer<N> private constructor(
    branchFunction: (N) -> List<N>,
    seed: N,
    private val _observableEntries: ObservableMutableList<TreeSliceEntry<N>>
) :
    TreeSliceViewer<N>(branchFunction, _observableEntries, seed),
    ObservableList<TreeSliceViewer.TreeSliceEntry<N>> by _observableEntries {
    override fun subList(fromIndex: Int, toIndex: Int): ObservableList<TreeSliceEntry<N>> =
        _observableEntries.subList(fromIndex, toIndex)

    override fun iterator(): Iterator<TreeSliceEntry<N>> = _observableEntries.iterator()
    override fun listIterator(): ListIterator<TreeSliceEntry<N>> = observableEntries.listIterator()
    override fun listIterator(index: Int): ListIterator<TreeSliceEntry<N>> = observableEntries.listIterator(index)
    override fun contains(element: TreeSliceEntry<N>): Boolean = _observableEntries.contains(element)
    override fun containsAll(elements: Collection<TreeSliceEntry<N>>): Boolean =
        _observableEntries.containsAll(elements)

    override fun get(index: Int): TreeSliceEntry<N> = _observableEntries[index]
    override fun indexOf(element: TreeSliceEntry<N>): Int = _observableEntries.indexOf(element)
    override fun isEmpty(): Boolean = _observableEntries.isEmpty()
    override fun lastIndexOf(element: TreeSliceEntry<N>): Int = _observableEntries.lastIndexOf(element)
    override val size: Int
        get() = _observableEntries.size

    constructor(brancher: IBranchProvider<N>, seed: N) : this(brancher.branchFunction, seed)
    constructor(brancher: ISeededBranchProvider<N>) : this(brancher.branchFunction, brancher.seed)
    constructor(branchFunction: (N) -> List<N>, seed: N) : this(
        branchFunction,
        seed,
        observableListOf(TreeSliceEntry(seed))
    )

    val observableEntries: ObservableList<TreeSliceEntry<N>> = _observableEntries

    val hasNextProperty: ObservableValue<Boolean> = property(observableEntries) {
        hasNext()
    }
    val hasPreviousProperty: ObservableValue<Boolean> = property(observableEntries) {
        hasPrevious()
    }
    val currentNodeProperty: ObservableValue<N> = property(observableEntries) {
        currentNode
    }
}
