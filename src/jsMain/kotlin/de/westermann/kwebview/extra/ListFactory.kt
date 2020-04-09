package de.westermann.kwebview.extra

import de.robolab.utils.runAfterTimeout
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.list.ObservableReadOnlyList
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
            listProperty: ReadOnlyProperty<ObservableReadOnlyList<T>>,
            factory: (T) -> V,
            animateAdd: Int? = null,
            animateRemove: Int? = null
    ) : this(container, factory, animateAdd, animateRemove) {
        this.listProperty.bind(listProperty)
        invalidate(listProperty.value)
    }

    constructor(
            container: ViewCollection<in V>,
            list: ObservableReadOnlyList<T>,
            factory: (T) -> V,
            animateAdd: Int? = null,
            animateRemove: Int? = null
    ) : this(container, factory, animateAdd, animateRemove) {
        listProperty.value = list
    }

    val listProperty: Property<ObservableReadOnlyList<T>> = property(observableListOf())
    val list by listProperty

    private val listenerReferences = mutableListOf<EventListener<*>>()

    fun invalidate(list: ObservableReadOnlyList<T>) {
        for (listener in listenerReferences) {
            listener.detach()
        }
        listenerReferences.clear()
        container.clear()

        for (element in list) {
            container += factory(element)
        }
        listenerReferences += list.onAdd.reference { (index, element) ->
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
        listenerReferences += list.onRemove.reference { (index) ->
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
        listenerReferences += list.onUpdate.reference { (oldIndex, newIndex, element) ->
            container.removeAt(oldIndex)
            container.add(newIndex, factory(element))
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
        listProperty: ReadOnlyProperty<ObservableReadOnlyList<T>>,
        factory: (T) -> V,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, listProperty, factory, animateAdd, animateRemove)

fun <V : View> ViewCollection<in V>.listFactory(
        listProperty: ReadOnlyProperty<ObservableReadOnlyList<V>>,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, listProperty, { it }, animateAdd, animateRemove)

fun <T, V : View> ViewCollection<in V>.listFactory(
        list: ObservableReadOnlyList<T>,
        factory: (T) -> V,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, list, factory, animateAdd, animateRemove)

fun <V : View> ViewCollection<in V>.listFactory(
        list: ObservableReadOnlyList<V>,
        animateAdd: Int? = null,
        animateRemove: Int? = null
) = ListFactory(this, list, { it }, animateAdd, animateRemove)
