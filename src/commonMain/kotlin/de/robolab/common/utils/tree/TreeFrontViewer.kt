package de.robolab.common.utils.tree

import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.delay
import kotlin.js.JsName
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

//Warning: The expand-methods in this interface may not be thread-safe / coroutine-safe. Only one expand-like method may
// run at once, but reading the front-list is always okay
interface ITreeFrontViewer<out N> {
    val front: List<N>
    val expandToBack: Boolean
    fun isExhausted(): Boolean
    fun expandAll(): Int
    suspend fun expandAllAsync(
        delay: Duration = PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
    ): Int

    suspend fun expandAllFully(
        depthFirst: Boolean
    )

    suspend fun expandAllFullyAsync(
        depthFirst: Boolean,
        delay: Duration = PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
    )

    fun expandAt(index: Int): Int
    fun expandAtFully(index: Int, depthFirst: Boolean): Int
    suspend fun expandAtFullyAsync(
        index: Int,
        depthFirst: Boolean,
        delay: Duration = PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
    ): Int

    fun expandNext(): Int
    fun expandNextFully(depthFirst: Boolean): Int
    suspend fun expandNextFullyAsync(
        depthFirst: Boolean,
        delay: Duration = PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
    ): Int
}

//Warning: The expand-methods in this class are not thread-safe / coroutine-safe. Only one expand-like method may run at
// once, but reading the front-list is always okay
open class TreeFrontViewer<N> protected constructor(
    private val expandFunction: (N) -> List<N>,
    override val expandToBack: Boolean = true,
    protected val pFront: MutableList<N>
) : ITreeFrontViewer<N> {
    protected open var pUncompletedIndex: Int = 0
    override val front: List<N> = pFront

    constructor(
        brancher: ISeededBranchProvider<N>,
        expandToBack: Boolean = true
    ) : this(brancher.branchFunction, expandToBack, mutableListOf(brancher.seed))

    constructor(
        brancher: ISeededMutableBranchProvider<N>,
        expandToBack: Boolean = true
    ) : this({
        val (addSelf, siblings) = brancher.expandFunction(it)
        if (addSelf) listOf(it) + siblings
        else siblings
    }, expandToBack, mutableListOf(brancher.seedFactory()))

    override fun isExhausted(): Boolean = pUncompletedIndex >= pFront.size

    override fun expandAll(): Int = expandAll(pUncompletedIndex)
    protected fun expandAll(startFrom: Int): Int {
        if (startFrom >= pFront.size) return 0
        val expandCount = pFront.size - startFrom
        var nextIndex = startFrom
        var additionalCount = 0
        for (i in 1..expandCount) {
            val newChildrenCount = expandAt(nextIndex)
            nextIndex += when {
                newChildrenCount == 0 -> 1
                expandToBack -> 1
                else -> newChildrenCount
            }
            additionalCount += newChildrenCount
        }
        return additionalCount
    }

    override suspend fun expandAllAsync(delay: Duration): Int = expandAllAsync(pUncompletedIndex, delay)
    protected suspend fun expandAllAsync(startFrom: Int, delay: Duration): Int {
        if (startFrom >= pFront.size) return 0
        val expandCount = pFront.size - startFrom
        var nextIndex = startFrom
        var additionalCount = 0
        for (i in 1..expandCount) {
            delay(delay)
            val newChildrenCount = expandAt(nextIndex)
            nextIndex += when {
                newChildrenCount == 0 -> 1
                expandToBack -> 1
                else -> newChildrenCount
            }
            additionalCount += newChildrenCount
        }
        return additionalCount
    }

    @Suppress("ControlFlowWithEmptyBody")
    override suspend fun expandAllFully(depthFirst: Boolean) {
        if (depthFirst)
            while (expandNextFully(true) > 0) {
            }
        else
            while (expandAll() > 0) {
            }
    }

    @Suppress("ControlFlowWithEmptyBody")
    override suspend fun expandAllFullyAsync(depthFirst: Boolean, delay: Duration) {
        if (depthFirst)
            while (expandNextFullyAsync(true, delay) > 0) {
            }
        else
            while (expandAllAsync(delay) > 0) {
            }
    }

    override fun expandAt(index: Int): Int {
        return if (index < pUncompletedIndex) 0
        else expand(pFront[index], index)
    }

    //TODO: Write tree-builder dsl and then tests for whatever this is
    override fun expandAtFully(index: Int, depthFirst: Boolean): Int {
        if (index < pUncompletedIndex) return 0
        val sizeBefore = pFront.size
        val initialChildCount = expandAt(index)
        if (initialChildCount == 0) return 0
        if (expandToBack) {
            //Children are placed at $sizeBefore
            if (depthFirst) {
                expandAtFully(index, true)
                for (i in sizeBefore until (sizeBefore + initialChildCount - 1)) { //-1 for child[0] staying at index
                    expandAtFully(i, true)
                }
            } else {
                do {
                    val selfChildCount = expandAt(index)
                    val childChildCount = expandAll(sizeBefore)
                } while (selfChildCount + childChildCount > 0)
            }
        } else {
            //Children are placed at ${index+1}
            if (depthFirst) {
                var nextChildIndex = index
                for (i in 1..initialChildCount) {
                    nextChildIndex += max(1, expandAtFully(nextChildIndex, true))
                }
            } else {
                var currentChildSize = initialChildCount
                do {
                    var nextChildIndex = index
                    var hasWorkLeft = false
                    for (i in 1..currentChildSize) {
                        val subChildCount = expandAt(nextChildIndex)
                        hasWorkLeft = hasWorkLeft || (subChildCount > 0)
                        nextChildIndex += max(
                            1,
                            subChildCount
                        ) //Skip at least current child, but also any of its children
                    }
                    currentChildSize = nextChildIndex - index
                } while (hasWorkLeft)
            }
        }
        return pFront.size - sizeBefore + 1 //+1 because of the initial node
    }

    override suspend fun expandAtFullyAsync(index: Int, depthFirst: Boolean, delay: Duration): Int {
        if (index < pUncompletedIndex) return 0
        val sizeBefore = pFront.size
        delay(delay)
        val initialChildCount = expandAt(index)
        if (initialChildCount == 0) return 0
        if (expandToBack) {
            //Children are placed at $sizeBefore
            if (depthFirst) {
                expandAtFullyAsync(index, true, delay)
                for (i in sizeBefore until (sizeBefore + initialChildCount - 1)) { //-1 for child[0] staying at index
                    expandAtFullyAsync(i, true, delay)
                }
            } else {
                do {
                    delay(delay)
                    val selfChildCount = expandAt(index)
                    val childChildCount = expandAllAsync(sizeBefore, delay)
                } while (selfChildCount + childChildCount > 0)
            }
        } else {
            //Children are placed at ${index+1}
            if (depthFirst) {
                var nextChildIndex = index
                for (i in 1..initialChildCount) {
                    nextChildIndex += max(1, expandAtFullyAsync(nextChildIndex, true, delay))
                }
            } else {
                var currentChildSize = initialChildCount
                do {
                    var nextChildIndex = index
                    var hasWorkLeft = false
                    for (i in 1..currentChildSize) {
                        delay(delay)
                        val subChildCount = expandAt(nextChildIndex)
                        hasWorkLeft = hasWorkLeft || (subChildCount > 0)
                        nextChildIndex += max(
                            1,
                            subChildCount
                        ) //Skip at least current child, but also any of its children
                    }
                    currentChildSize = nextChildIndex - index
                } while (hasWorkLeft)
            }
        }
        return pFront.size - sizeBefore + 1 //+1 because of the initial node
    }

    fun expand(element: N): Int {
        val index = pFront.indexOf(element)
        if (index < 0) throw NoSuchElementException()
        return if (index < pUncompletedIndex) 0
        else expand(element, index)
        //Considered second lookup after restricted to after the _uncompletedIndex, but any other element found would
        //have to be equal to the first one found and as such also have no children.
    }

    private fun expand(element: N, index: Int): Int {
        val children = expandFunction(element)
        if (children.isEmpty()) {
            if (index == pUncompletedIndex)
                pUncompletedIndex++
            return 0
        }
        pFront[index] = children[0]
        val followChildren = children.subList(1, children.size)
        if (expandToBack) {
            pFront.addAll(followChildren)
        } else {
            pFront.addAll(index + 1, followChildren)
        }
        return children.size
    }

    override fun expandNext(): Int {
        val nextIndex = pUncompletedIndex
        return if (nextIndex < pFront.size) expandAt(nextIndex) else 0
    }

    override fun expandNextFully(depthFirst: Boolean): Int =
        if (pUncompletedIndex < pFront.size) expandAtFully(pUncompletedIndex, depthFirst) else 0

    @Suppress("ControlFlowWithEmptyBody")
    override suspend fun expandNextFullyAsync(depthFirst: Boolean, delay: Duration): Int =
        if (pUncompletedIndex < pFront.size) expandAtFullyAsync(pUncompletedIndex, depthFirst, delay) else 0
}

class ObservableTreeFrontViewer<N> private constructor(
    expandFunction: (N) -> List<N>,
    expandToBack: Boolean = true,
    _observableFront: ObservableMutableList<N>
) : TreeFrontViewer<N>(expandFunction, expandToBack, _observableFront), ObservableList<N> by _observableFront {

    constructor(
        brancher: ISeededBranchProvider<N>,
        expandToBack: Boolean = true
    ) : this(brancher.branchFunction, expandToBack, observableListOf(brancher.seed))

    constructor(
        brancher: ISeededMutableBranchProvider<N>,
        expandToBack: Boolean = true
    ) : this({
        val (addSelf, siblings) = brancher.expandFunction(it)
        if (addSelf) listOf(it) + siblings
        else siblings
    }, expandToBack, observableListOf(brancher.seedFactory()))

    companion object {
        fun <N> fromBranchProvider(brancher: ISeededBranchProvider<N>, expandToBack: Boolean = true) =
            ObservableTreeFrontViewer(brancher, expandToBack)

        fun <N> fromMutableBranchProvider(brancher: ISeededMutableBranchProvider<N>, expandToBack: Boolean = true) =
            ObservableTreeFrontViewer(brancher, expandToBack)
    }

    val observableFront: ObservableList<N> = _observableFront

    private val _uncompletedIndex: ObservableProperty<Int> = property(0)
    override var pUncompletedIndex: Int by _uncompletedIndex
    @JsName("isExhaustedProp")
    val isExhausted: ObservableValue<Boolean> =
        property(_uncompletedIndex, observableFront) { _uncompletedIndex.value >= observableFront.size }

}
