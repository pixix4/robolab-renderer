package de.robolab.client.app.model.room

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.IPlanetProvider
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.property

class RoomPlanetProvider : IPlanetProvider {

    override val searchStringProperty =
        property("")
    override val entryList =
        property(observableListOf<INavigationBarEntry>())
}
