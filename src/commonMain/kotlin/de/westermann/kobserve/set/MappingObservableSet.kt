package de.westermann.kobserve.set

import de.westermann.kobserve.base.ObservableSet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property

class MappingObservableSet<P, T>(
        private val parent: ObservableSet<P>,
        transformation: (P) -> T
) : BaseObservableSet<T>(parent.map { transformation(it) }.toMutableSet()) {

    val transformationProperty = property(transformation)
    var transformation by transformationProperty

    override fun invalidate() {
        val newSet = parent.map { transformation(it) }.toSet()

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
        parent.onAdd { parentElement ->
            val element = transformation(parentElement)
            if (backingField.add(element)) {
                emitOnAdd(element)
            }
        }

        parent.onRemove { parentElement ->
            val element = transformation(parentElement)

            if (element in backingField && parent.none { transformation(it) == element }) {
                backingField.remove(element)
                emitOnRemove(element)
            }
        }

        parent.onClear {
            val elements = backingField.toSet()
            backingField.clear()
            emitOnClear(elements)
        }

        transformationProperty.onChange {
            invalidate()
        }
    }
}

fun <P, T> ObservableSet<P>.mapObservable(transformation: (P) -> T): ObservableSet<T> =
        MappingObservableSet(this, transformation)

fun <P, T> ObservableSet<P>.mapObservable(transformationProperty: ObservableValue<(P) -> T>): ObservableSet<T> =
        MappingObservableSet(this, transformationProperty.value).also {
            it.transformationProperty.bind(transformationProperty)
        }
