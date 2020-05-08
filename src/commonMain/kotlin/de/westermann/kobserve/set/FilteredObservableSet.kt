package de.westermann.kobserve.set

import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property

class FilteredObservableSet<T>(
        private val parent: ObservableSet<T>,
        predicate: (T) -> Boolean
) : BaseObservableSet<T>(parent.filter(predicate).toMutableSet()) {

    val predicateProperty = property(predicate)
    var predicate by predicateProperty

    override fun invalidate() {
        val newSet = parent.filter(predicate).toSet()

        if (newSet == backingField) return

        val elementsToRemove = backingField - newSet
        val elementsToAdd = newSet - backingField

        if (elementsToRemove == backingField) {
            backingField.clear()
            emitOnClear(elementsToRemove)
        }

        for (element in elementsToAdd) {
            backingField += element
            emitOnAdd(element)
        }
    }

    init {
        parent.onAdd { element ->
            if (predicate(element) && backingField.add(element)) {
                emitOnAdd(element)
            }
        }

        parent.onRemove { element ->
            if (backingField.remove(element)) {
                emitOnRemove(element)
            }
        }

        parent.onClear {
            val elements = backingField.toSet()
            backingField.clear()
            emitOnClear(elements)
        }

        predicateProperty.onChange {
            invalidate()
        }
    }
}

fun <T> ObservableSet<T>.filterObservable(predicate: (T) -> Boolean): ObservableSet<T> =
        FilteredObservableSet(this, predicate)

fun <T> ObservableSet<T>.filterObservable(predicateProperty: ObservableValue<(T) -> Boolean>): ObservableSet<T> =
        FilteredObservableSet(this, predicateProperty.value).also {
            it.predicateProperty.bind(predicateProperty)
        }
