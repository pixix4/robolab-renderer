package de.robolab.client.app.controller

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.nullableFlatMapBinding

class InfoBarController(
    private val activeDocumentProperty: ObservableValue<IPlanetDocument?>
) {

    val infoBarContentProperty = activeDocumentProperty
        .nullableFlatMapBinding { it?.infoBarProperty }

    val infoBarTabsProperty = activeDocumentProperty
        .nullableFlatMapBinding { it?.infoBarTabsProperty }

    val infoBarActiveTabProperty = activeDocumentProperty
        .nullableFlatMapBinding { it?.infoBarActiveTabProperty }

    interface Tab {
        val icon: MaterialIcon
        val tooltip: String
        fun open()
    }
}
