package de.robolab.jfx.adapter

import de.westermann.kobserve.Binding
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.event.EventHandler
import javafx.beans.value.ObservableValue

open class FxObservable<T>(private val prop: ObservableValue<T>) : ReadOnlyProperty<T> {
    override val onChange = EventHandler<Unit>()

    override fun get(): T {
        return prop.value
    }

    init {
        prop.addListener { _ ->
            onChange.emit(Unit)
        }
    }
}

fun <T> ObservableValue<T>.toReadOnlyProperty(): ReadOnlyProperty<T> = FxObservable(this)

class FxProperty<T>(private val prop: javafx.beans.property.Property<T>) : FxObservable<T>(prop), Property<T> {
    override fun set(value: T) {
        super.set(value)
        prop.value = value
    }

    override var binding: Binding<T> = Binding.Unbound()
}

fun <T> javafx.beans.property.Property<T>.toProperty(): Property<T> = FxProperty(this)
