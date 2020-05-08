package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property

class FilteredObservableList<T>(
        parent: ObservableList<T>,
        predicate: (T) -> Boolean
) : RelationalObservableList<T>(parent) {

    val predicateProperty = property(predicate)
    var predicate by predicateProperty

    override fun createRelation(): Sequence<Relation<T>> {
        return parent
                .asSequence()
                .withIndex()
                .filter { predicate(it.value) }
                .map { Relation(it.index, it.value) }
    }

    init {
        relation.addAll(createRelation())

        parent.onAddIndex { (index, element) ->
            var insertIndex = relation.indexOfFirst {
                it.index >= index
            }
            if (insertIndex < 0) {
                insertIndex = size
            }

            for (i in insertIndex until size) {
                relation[i].index += 1
            }

            if (predicate(element)) {
                relation.add(insertIndex, Relation(index, element))
                emitOnAdd(insertIndex, element)
            }
        }

        parent.onSetIndex { (index, oldElement, newElement) ->
            val relationIndex = relation.indexOfFirst { it.index == index }

            if (relationIndex >= 0) {
                if (predicate(newElement)) {
                    relation[relationIndex].element = newElement
                    emitOnSet(relationIndex, oldElement, newElement)
                } else {
                    relation.removeAt(relationIndex)
                    emitOnRemove(relationIndex, oldElement)
                }
            } else {
                if (predicate(newElement)) {
                    var insertIndex = relation.indexOfFirst {
                        it.index >= index
                    }
                    if (insertIndex < 0) {
                        insertIndex = size
                    }

                    relation.add(insertIndex, Relation(index, newElement))
                    emitOnAdd(insertIndex, newElement)
                }
            }
        }

        parent.onRemoveIndex { (index, element) ->
            val removeIndex = relation.indexOfFirst { it.index >= index }
            if (removeIndex >= 0) {
                for (i in removeIndex until size) {
                    relation[i].index -= 1
                }

                if (predicate(element)) {
                    relation.removeAt(removeIndex)
                    emitOnRemove(removeIndex, element)
                }
            }
        }

        parent.onClear { elements ->
            relation.clear()
            emitOnClear(elements.filter(predicate))
        }

        predicateProperty.onChange {
            invalidate()
        }
    }
}

fun <T> ObservableList<T>.filterObservable(predicate: (T) -> Boolean): FilteredObservableList<T> =
        FilteredObservableList(this, predicate)

fun <T> ObservableList<T>.filterObservable(predicateProperty: ObservableValue<(T) -> Boolean>): FilteredObservableList<T> =
        FilteredObservableList(this, predicateProperty.value).also {
            it.predicateProperty.bind(predicateProperty)
        }

fun <T, F> ObservableList<T>.filterObservable(
        filterProperty: ObservableValue<F>,
        predicate: (element: T, filter: F) -> Boolean
): FilteredObservableList<T> = FilteredObservableList(this) { predicate(it, filterProperty.value) }.also { list ->
    filterProperty.onChange { list.invalidate() }
}
