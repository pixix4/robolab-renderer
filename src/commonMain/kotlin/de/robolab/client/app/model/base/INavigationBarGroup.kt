package de.robolab.client.app.model.base

import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue

interface INavigationBarGroup : INavigationBarEntry {

    val entryList: ObservableValue<ObservableList<INavigationBarEntry>>
}
