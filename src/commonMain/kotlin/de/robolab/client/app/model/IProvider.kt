package de.robolab.client.app.model

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty

interface IProvider {

    val searchStringProperty: ObservableProperty<String>
    val entryList: ObservableList<ISideBarEntry>
}
