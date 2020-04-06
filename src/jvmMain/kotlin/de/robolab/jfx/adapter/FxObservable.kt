package de.robolab.jfx.adapter

import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.ObservableReadOnlyList
import javafx.beans.value.ObservableValue
import javafx.beans.value.ObservableValueBase
import javafx.collections.ObservableList
import javafx.collections.ObservableListBase
import tornadofx.*

class FxObservableValue<T>(private val property: ReadOnlyProperty<T>) : ObservableValueBase<T>() {
    override fun getValue(): T {
        return property.value
    }

    init {
        property.onChange {
            fireValueChangedEvent()
        }
    }
}

fun <T> ReadOnlyProperty<T>.toFx(): ObservableValue<T> = FxObservableValue(this)

class FxObservableList<T>(private val list: ObservableReadOnlyList<T>) : ObservableListBase<T>() {
    override fun get(index: Int): T {
        return list[index]
    }

    override val size: Int
        get() = list.size

    init {
        list.onAdd { (index, _) ->
            beginChange()
            nextAdd(index, index)
            endChange()
        }
        list.onUpdate { (oldIndex, newIndex, element) ->
            beginChange()
            nextRemove(oldIndex, element)
            nextAdd(newIndex, newIndex)
            endChange()
        }
        list.onRemove { (index, element) ->
            beginChange()
            nextRemove(index, element)
            endChange()
        }
    }
}

fun <T> ObservableReadOnlyList<T>.toFx(): ObservableList<T> = FxObservableList(this)

class FxProperty<T>(private val property: Property<T>) : javafx.beans.property.SimpleObjectProperty<T>() {
    init {
        set(property.value)

        property.onChange {
            set(property.value)
        }

        onChange {
            property.value = get()
        }
    }
}

fun <T> Property<T>.toFx(): javafx.beans.property.Property<T> = FxProperty(this)
