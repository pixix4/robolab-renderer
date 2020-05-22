package de.westermann.kobserve.property

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit

open class FunctionObservableValue<T>(
        private val delegateAccessor: DelegateValueAccessor<T>
) : ObservableValue<T> {
    override val onChange = EventHandler<Unit>()

    protected open var internal: T = delegateAccessor.get()

    override fun get() = internal

    override fun invalidate() {
        val newValue = delegateAccessor.get()
        if (newValue != internal) {
            internal = newValue
            onChange.emit()
        }
    }

    fun listenTo(vararg properties: ObservableValue<*>) {
        for (property in properties) {
            property.onChange {
                invalidate()
            }
        }
    }

    constructor(
            functionAccessor: DelegateValueAccessor<T>,
            vararg properties: ObservableValue<*>
    ) : this(functionAccessor) {
        listenTo(*properties)
    }
}

interface DelegateValueAccessor<T> {
    /**
     * Perform the get operation of the property.
     */
    fun get(): T
}

/**
 * Create a readonly property that calls the given function accessor on every get operation.
 *
 * @param functionAccessor The function accessor to call.
 * @param properties The new property will listen to their onChange events.
 */
fun <T> property(
        functionAccessor: DelegateValueAccessor<T>,
        vararg properties: ObservableValue<*>
): ObservableValue<T> = FunctionObservableValue(functionAccessor, *properties)

/**
 * Create a readonly property that calls the given function accessor on every get operation.
 *
 * @param properties The new property will listen to their onChange events.
 * @param accessor The function accessor to call.
 */
fun <T> property(vararg properties: ObservableValue<*>, accessor: () -> T): ObservableValue<T> =
        FunctionObservableValue(object : DelegateValueAccessor<T> {
            override fun get(): T = accessor()
        }, *properties)

/**
 * Convenient function to map two properties together. The new property will call the given block with both property
 * values as parameter and return the result as its value.
 */
fun <A, B, C> ObservableValue<A>.join(
        property2: ObservableValue<B>,
        block: (A, B) -> C
): ObservableValue<C> {
    return FunctionObservableValue(object : DelegateValueAccessor<C> {
        override fun get(): C {
            return block(this@join.value, property2.value)
        }
    }, this, property2)
}
