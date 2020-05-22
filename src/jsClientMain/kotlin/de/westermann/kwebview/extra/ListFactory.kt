package de.westermann.kwebview.extra

import de.robolab.client.utils.runAfterTimeout
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection

class ListFactory<T, V : View>(
        private val container: ViewCollection<in V>,
        private val factory: (T) -> V,
        private val animateAdd: Int? = null,
        private val animateRemove: Int? = null
) {

    constructor(
            container: ViewCollection<in V>,
            listProperty: ObservableValue<ObservableList<T>>,
            factory: (T) -> V,
            animateAdd: Int? = null,
            animateRemove: Int? = null
    ) : this(container, factory, animateAdd, animateRemove) {
        this.listProperty.bind(listProperty)
        invalidate(listProperty.value)
    }

    constructor(
            container: ViewCollection<in V>,
            list: ObservableList<T>,
            factory: (T) -> V,
            animateAdd: Int? = null,
            animateRemove: Int? = null
    ) : this(container, factory, animateAdd, animateRemove) {
        listProperty.value = list
    }

    val listProperty: ObservableProperty<ObservableList<T>> = property(observableListOf())
    val list by listProperty

    private val listenerReferences = mutableListOf<EventListener<*>>()

    fun invalidate(list: ObservableList<T>) {
        for (listener in listenerReferences) {
            listener.detach()
        }
        listenerReferences.clear()
        container.clear()

        for (element in list) {
            container += factory(element)
        }
        listenerReferences += list.onAddIndex.reference { (index, element) ->
            val view = factory(element)
            container.add(index, view)

            if (animateAdd != null) {
                container.classList += "animate-add"
                view.classList += "active"

                runAfterTimeout(animateAdd) {
                    container.classList -= "animate-add"
                    view.classList -= "active"
                }
            }
        }
        listenerReferences += list.onRemoveIndex.reference { (index) ->
            @Suppress("UNCHECKED_CAST") val view = container[index] as V

            if (animateRemove == null) {
                container -= view
            } else {
                container.classList += "animate-remove"
                view.classList += "active"

                runAfterTimeout(animateRemove) {
                    container.classList -= "animate-remove"
                    view.classList -= "active"
                    container -= view
                }
            }
        }
        listenerReferences += list.onSetIndex.reference { (index, _, newElement) ->
            container.removeAt(index)
            container.add(index, factory(newElement))
        }
        listenerReferences += list.onClear.reference {
            container.clear()
        }
    }

    init {
        invalidate(list)
        listProperty.onChange {
            invalidate(list)
        }
    }
}

fun <T, V : View> ViewCollection<in V>.listFactory(
        listProperty: ObservableValue<ObservableList<T>>,
        factory: (T) -> V,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, listProperty, factory, animateAdd, animateRemove)

fun <V : View> ViewCollection<in V>.listFactory(
        listProperty: ObservableValue<ObservableList<V>>,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, listProperty, { it }, animateAdd, animateRemove)

fun <T, V : View> ViewCollection<in V>.listFactory(
        list: ObservableList<T>,
        factory: (T) -> V,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, list, factory, animateAdd, animateRemove)

fun <V : View> ViewCollection<in V>.listFactory(
        list: ObservableList<V>,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, list, { it }, animateAdd, animateRemove)
