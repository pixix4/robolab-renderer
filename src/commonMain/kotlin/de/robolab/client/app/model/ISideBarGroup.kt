package de.robolab.client.app.model

import de.westermann.kobserve.base.ObservableList

interface ISideBarGroup : ISideBarEntry {

    val entryList: ObservableList<ISideBarEntry>
}
