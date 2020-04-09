package de.robolab.app.model

import de.westermann.kobserve.list.ObservableReadOnlyList

interface IProvider {

    val entryList: ObservableReadOnlyList<ISideBarEntry>
}
