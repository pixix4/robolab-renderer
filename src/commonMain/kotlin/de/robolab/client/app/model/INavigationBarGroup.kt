package de.robolab.client.app.model

import de.westermann.kobserve.base.ObservableList

interface INavigationBarGroup : INavigationBarEntry {

    val entryList: ObservableList<INavigationBarEntry>
}
