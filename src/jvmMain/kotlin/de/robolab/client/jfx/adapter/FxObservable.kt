package de.robolab.client.jfx.adapter

import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.ContextMenuAction
import de.robolab.client.utils.ContextMenuEntry
import de.robolab.client.utils.ContextMenuList
import de.westermann.kobserve.base.ObservableProperty
import javafx.beans.value.ObservableValue
import javafx.beans.value.ObservableValueBase
import javafx.collections.ObservableList
import javafx.collections.ObservableListBase
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import tornadofx.onChange

class FxObservableValue<T>(private val property: de.westermann.kobserve.base.ObservableValue<T>) : ObservableValueBase<T>() {
    override fun getValue(): T {
        return property.value
    }

    init {
        property.onChange {
            fireValueChangedEvent()
        }
    }
}

fun <T> de.westermann.kobserve.base.ObservableValue<T>.toFx(): ObservableValue<T> = FxObservableValue(this)

class FxObservableList<T>(private val list: de.westermann.kobserve.base.ObservableList<T>) : ObservableListBase<T>() {
    override fun get(index: Int): T {
        return list[index]
    }

    override val size: Int
        get() = list.size

    init {
        list.onAddIndex { (index, _) ->
            beginChange()
            nextAdd(index, index)
            endChange()
        }
        list.onSetIndex { (index, oldElement) ->
            beginChange()
            nextSet(index, oldElement)
            endChange()
        }
        list.onRemoveIndex { (index, element) ->
            beginChange()
            nextRemove(index, element)
            endChange()
        }
        list.onClear {
            beginChange()
            nextRemove(0, it.toList())
            endChange()
        }
    }
}

fun <T> de.westermann.kobserve.base.ObservableList<T>.toFx(): ObservableList<T> = FxObservableList(this)

class FxProperty<T>(private val property: ObservableProperty<T>) : javafx.beans.property.SimpleObjectProperty<T>() {
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

fun <T> ObservableProperty<T>.toFx(): javafx.beans.property.Property<T> = FxProperty(this)


fun ContextMenu.toFx(): javafx.scene.control.ContextMenu {
    return javafx.scene.control.ContextMenu(
            *entry.entries.map { it.toFx() }.toTypedArray()
    )
}

fun ContextMenuEntry.toFx(): MenuItem {
    when (this) {
        is ContextMenuList -> {
            val menu = Menu(label)

            menu.items.addAll(entries.map { it.toFx() })

            return menu
        }
        is ContextMenuAction -> {
            return if (checked == null) {
                MenuItem(label).also {
                    it.setOnAction {
                        action()
                    }
                }
            } else {
                CheckMenuItem(label).also {
                    it.isSelected = checked
                    it.setOnAction {
                        action()
                    }
                }
            }
        }
        else -> throw IllegalArgumentException()
    }
}
