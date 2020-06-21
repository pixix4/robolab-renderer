package de.robolab.client.app.model.base

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue

interface IPlanetProvider {

    val searchStringProperty: ObservableProperty<String>
    val entryList: ObservableValue<ObservableList<INavigationBarEntry>>
}
