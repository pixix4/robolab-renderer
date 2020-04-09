package de.robolab.app.model

import de.westermann.kobserve.list.ObservableReadOnlyList

interface ISideBarGroup: ISideBarEntry {

    val entryList: ObservableReadOnlyList<ISideBarEntry>
}
