package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import kotlin.math.min

class MappingObservableList<P, T>(
        private val parent: ObservableList<P>,
        transformation: (P) -> T
) : BaseObservableList<T>(parent.map(transformation).toMutableList()) {

    val transformationProperty = property(transformation)
    var transformation by transformationProperty

    override fun invalidate() {
        val newList = parent.map(transformation)

        if (newList.isEmpty()) {
            val elements = backingField.toList()
            backingField.clear()
            emitOnClear(elements)
            return
        }

        val minSize = min(newList.size, backingField.size)
        for (index in 0 until minSize) {
            val newElement = newList[index]
            val oldElement = backingField.set(index, newElement)
            if (oldElement != newElement) {
                emitOnSet(index, oldElement, newElement)
            }
        }

        for (index in minSize until newList.size) {
            val element = newList[index]
            backingField.add(element)
            emitOnAdd(index, element)
        }

        for (index in backingField.lastIndex downTo minSize) {
            val element = backingField.removeAt(index)
            emitOnRemove(index, element)
        }
    }

    init {
        parent.onAddIndex { (index, parentElement) ->
            val element = transformation(parentElement)
            backingField.add(index, element)
            emitOnAdd(index, element)
        }

        parent.onSetIndex { (index, _, newParentElement) ->
            val newElement = transformation(newParentElement)

            val oldElement = backingField.set(index, newElement)

            emitOnSet(index, oldElement, newElement)
        }

        parent.onRemoveIndex { (index, _) ->
            val element = backingField.removeAt(index)
            emitOnRemove(index, element)
        }

        parent.onClear {
            val elements = backingField.toList()
            backingField.clear()
            emitOnClear(elements)
        }

        transformationProperty.onChange {
            invalidate()
        }
    }
}

fun <P, T> ObservableList<P>.mapObservable(
        transformation: (P) -> T
): ObservableList<T> = MappingObservableList(this, transformation)

fun <P, T> ObservableList<P>.mapObservable(
        transformationProperty: ObservableValue<(P) -> T>
): ObservableList<T> = MappingObservableList(this, transformationProperty.value).also {
    it.transformationProperty.bind(transformationProperty)
}
