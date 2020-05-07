package de.robolab.app.controller

import de.robolab.app.model.IInfoBarContent
import de.robolab.app.model.ISideBarPlottable
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property

class InfoBarController(private val selectedEntryProperty: ObservableProperty<ISideBarPlottable?>) {

    val contentListProperty = selectedEntryProperty.mapBinding { it?.infoBarList ?: emptyList() }

    val selectedContentIndexProperty = selectedEntryProperty
            .nullableFlatMapBinding { it?.selectedInfoBarIndexProperty }

    val selectedContentProperty = property(contentListProperty, selectedContentIndexProperty) {
        selectedContentIndexProperty.value?.let { contentListProperty.value.getOrNull(it) }
    }

    fun selectContent(content: IInfoBarContent) {
        val index = contentListProperty.value.indexOf(content)
        selectedEntryProperty.value?.selectedInfoBarIndexProperty?.value = index
    }
}
