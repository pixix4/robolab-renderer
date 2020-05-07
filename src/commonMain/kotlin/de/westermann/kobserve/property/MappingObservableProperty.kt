package de.westermann.kobserve.property

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import kotlin.reflect.KMutableProperty1

class MappingObservableProperty<R, T>(
    transform: (R) -> T,
    private val setter: (R, T) -> Unit,
    private val dependency: ObservableValue<R>
) : MappingObservableValue<R, T>(transform, dependency), ObservableProperty<T> {

    override var binding: Binding<T> = Binding.Unbound()

    override fun set(value: T) {
        super.set(value)
        if (internal != value) {
            setter(dependency.value, value)
            invalidate()
        }
    }

    override fun invalidate() {
        super<MappingObservableValue>.invalidate()
    }
}

/**
 * Apply a transform function to the given property value and return a readonly property for the transformed value.
 * The returned property supports invalidation.
 */
fun <R, T> ObservableProperty<R>.mapMutableBinding(transform: (R) -> T, reverseTransform: (T) -> R): ObservableProperty<T> =
    MappingObservableProperty(transform, { _, value -> this.set(reverseTransform(value)) }, this)

/**
 * Maps the given property to an readonly field attribute.
 * The returned property supports invalidation.
 *
 * @param attribute The readonly field attribute.
 */
fun <T, R> ObservableValue<R>.mapMutableBinding(attribute: KMutableProperty1<R, T>): ObservableProperty<T> =
    MappingObservableProperty(attribute::get, attribute::set, this)
