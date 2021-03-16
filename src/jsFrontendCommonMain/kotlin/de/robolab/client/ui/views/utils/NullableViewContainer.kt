package de.robolab.client.ui.views.utils

import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection

class NullableViewContainer<T>(
    private val traverserProperty: ObservableValue<T?>,
    private val viewFactory: (ObservableValue<T>) -> View
) : ViewCollection<View>() {

    private var prop: ObservableProperty<T>? = null
    private var view: View? = null

    private fun updateView() {
        val traverser = traverserProperty.value

        if (traverser == null) {
            clear()
            return
        }

        if (prop == null) {
            prop = property(traverser)
        } else {
            prop?.value = traverser
        }
        if (view == null) {
            view = viewFactory(prop!!)
        }

        add(view!!)
    }

    init {
        traverserProperty.onChange {
            updateView()
        }
        updateView()
    }
}
