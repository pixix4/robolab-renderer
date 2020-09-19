package de.westermann.kobserve.list

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener
import dev.gitlive.difflib.DiffUtils

fun<T> MutableList<T>.sync(other: List<T>) {
    val patch = DiffUtils.diff(this, other)

    for (delta in patch.getDeltas().asReversed()) {
        delta.applyTo(this)
    }
}

fun<T> ObservableValue<ObservableList<T>>.flattenListBinding(): ObservableList<T> {
    val list = observableListOf<T>()

    list.addAll(value)
    var handler: EventListener<*> = value.onChange.reference {
        list.sync(value)
    }

    onChange {
        handler.detach()
        list.sync(value)
        handler = value.onChange.reference {
            list.sync(value)
        }
    }

    return list
}
