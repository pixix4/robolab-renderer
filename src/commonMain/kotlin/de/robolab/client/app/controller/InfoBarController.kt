package de.robolab.client.app.controller

import de.robolab.client.app.model.base.INavigationBarPlottable
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.nullableFlatMapBinding

class InfoBarController(private val selectedEntryProperty: ObservableProperty<INavigationBarPlottable?>) {

    val selectedContentProperty = selectedEntryProperty.nullableFlatMapBinding { it?.infoBarProperty }

    val detailBoxProperty = selectedEntryProperty.nullableFlatMapBinding { it?.detailBoxProperty }
}
