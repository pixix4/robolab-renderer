package de.westermann.kobserve.property

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler

class ConstantObservableValue<T>(private val internal: T) : ObservableValue<T> {

    override val onChange = EventHandler<Unit>()

    override fun get(): T = internal
}

/**
 * Create an constant property that cannot change.
 *
 * @param value The constant value.
 */
fun <T> constObservable(value: T): ObservableValue<T> = ConstantObservableValue(value)

/**
 * Create an constant property that cannot change.
 *
 * @receiver value The constant value.
 */
fun <T> T.observeConst(): ObservableValue<T> = ConstantObservableValue(this)
