package de.robolab.client.app.controller

import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.INavigationBarPlottable
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property

class InfoBarController(private val selectedEntryProperty: ObservableProperty<INavigationBarPlottable?>) {

    val contentListProperty = selectedEntryProperty.mapBinding { it?.infoBarList ?: emptyList() }

    val selectedContentIndexProperty = selectedEntryProperty
            .nullableFlatMapBinding { it?.selectedInfoBarIndexProperty }

    val selectedContentProperty = property(contentListProperty, selectedContentIndexProperty) {
        selectedContentIndexProperty.value?.let { contentListProperty.value.getOrNull(it) }
    }

    val detailBoxProperty = selectedEntryProperty.nullableFlatMapBinding { it?.detailBoxProperty }

    fun selectContent(content: IInfoBarContent) {
        val index = contentListProperty.value.indexOf(content)
        selectedEntryProperty.value?.selectedInfoBarIndexProperty?.value = index
    }
}
