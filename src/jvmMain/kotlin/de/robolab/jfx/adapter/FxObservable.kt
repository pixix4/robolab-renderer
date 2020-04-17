package de.robolab.jfx.adapter

import de.robolab.utils.ContextMenu
import de.robolab.utils.ContextMenuAction
import de.robolab.utils.ContextMenuEntry
import de.robolab.utils.ContextMenuList
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.ObservableReadOnlyList
import javafx.beans.value.ObservableValue
import javafx.beans.value.ObservableValueBase
import javafx.collections.ObservableList
import javafx.collections.ObservableListBase
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
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


fun ContextMenu.toFx(): javafx.scene.control.ContextMenu {
    return javafx.scene.control.ContextMenu(
            *entry.entries.map { it.toFx() }.toTypedArray()
    )
}

fun ContextMenuEntry.toFx(): MenuItem {
    if (this is ContextMenuList) {
        val menu = Menu(label)

        menu.items.addAll(entries.map { it.toFx() })

        return menu
    } else if (this is ContextMenuAction) {
        return MenuItem(label).also {
            it.setOnAction {
                action()
            }
        }
    }

    throw IllegalArgumentException()
}
