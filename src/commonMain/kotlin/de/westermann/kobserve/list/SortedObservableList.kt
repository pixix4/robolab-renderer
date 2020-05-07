package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class SortedObservableList<T>(
    parent: ObservableList<T>,
    comparator: Comparator<T>
) : RelationalObservableList<T>(parent) {

    val comparatorProperty = property(comparator)
    var comparator by comparatorProperty

    private val relationComparator by comparatorProperty.mapBinding { c ->
        RelationComparator(c)
    }

    override fun createRelation(): Sequence<Relation<T>> {
        return (0 until parent.size).asSequence()
            .map { Relation(it, parent[it]) }
            .sortedWith(relationComparator)
    }

    private fun addIndex(index: Int, element: T): Int {
        val r = Relation(index, element)
        var insertIndex = size

        for (i in 0 until size) {
            if (relation[i].index >= index) {
                relation[i].index += 1
            }

            if (i < insertIndex && relationComparator.compare(r, relation[i]) <= 0) {
                insertIndex = i
            }
        }

        relation.add(insertIndex, Relation(index, element))
        return insertIndex
    }

    private fun removeIndex(index: Int): Int {
        var removeIndex = -1

        for (i in 0 until size) {
            if (removeIndex < 0 && relation[i].index == index) {
                removeIndex = i
            }

            if (relation[i].index > index) {
                relation[i].index -= 1
            }
        }
        check(removeIndex >= 0) { "No element was removed!" }

        relation.removeAt(removeIndex)
        return removeIndex
    }

    init {
        relation.addAll(createRelation())

        parent.onAddIndex { (index, element) ->
            val insertIndex = addIndex(index, element)

            emitOnAdd(insertIndex, element)
        }

        parent.onSetIndex { (index, oldElement, newElement) ->
            val removeIndex = removeIndex(index)
            val insertIndex = addIndex(index, newElement)

            if (removeIndex == insertIndex) {
                emitOnSet(insertIndex, oldElement, newElement)
            } else {
                emitOnRemove(removeIndex, oldElement)
                emitOnAdd(insertIndex, newElement)
            }
        }

        parent.onRemoveIndex { (index, element) ->
            val removeIndex = removeIndex(index)

            emitOnRemove(removeIndex, element)
        }

        parent.onClear { elements ->
            relation.clear()
            emitOnClear(elements.sortedWith(comparator))
        }

        comparatorProperty.onChange {
            invalidate()
        }
    }

    private class RelationComparator<T>(
        private val comparator: Comparator<T>
    ) : Comparator<Relation<T>> {

        override fun compare(a: Relation<T>, b: Relation<T>): Int {
            val result = comparator.compare(a.element, b.element)

            if (result != 0) return result

            return a.index - b.index
        }
    }
}

fun <T : Comparable<T>> ObservableList<T>.sortObservable(): SortedObservableList<T> =
    SortedObservableList(this, compareBy { it })

fun <T : Comparable<T>> ObservableList<T>.sortDescendingObservable(): SortedObservableList<T> =
    SortedObservableList(this, compareByDescending { it })

fun <T, R : Comparable<R>> ObservableList<T>.sortByObservable(selector: (T) -> R): SortedObservableList<T> =
    SortedObservableList(this, compareBy(selector))

fun <T, R : Comparable<R>> ObservableList<T>.sortByDescendingObservable(selector: (T) -> R): SortedObservableList<T> =
    SortedObservableList(this, compareByDescending(selector))

fun <T> ObservableList<T>.sortWithObservable(comparator: Comparator<T>): SortedObservableList<T> =
    SortedObservableList(this, comparator)

fun <T> ObservableList<T>.sortWithObservable(comparatorProperty: ObservableValue<Comparator<T>>): SortedObservableList<T> =
    SortedObservableList(this, comparatorProperty.value).also {
        it.comparatorProperty.bind(comparatorProperty)
    }
