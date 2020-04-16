package de.robolab.app.model

import de.westermann.kobserve.Property
import de.westermann.kobserve.list.ObservableReadOnlyList

interface IProvider {

    val searchStringProperty: Property<String>
    val entryList: ObservableReadOnlyList<ISideBarEntry>
}
