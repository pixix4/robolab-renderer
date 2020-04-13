package de.robolab.app.controller

import de.robolab.app.model.IInfoBarContent
import de.robolab.app.model.ISideBarPlottable
import de.westermann.kobserve.Property
import de.westermann.kobserve.property.flatMapReadOnlyNullableBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class InfoBarController(private val selectedEntryProperty: Property<ISideBarPlottable?>) {

    val contentListProperty = selectedEntryProperty.mapBinding { it?.infoBarList ?: emptyList() }

    val selectedContentIndexProperty = selectedEntryProperty
            .flatMapReadOnlyNullableBinding { it?.selectedInfoBarIndexProperty }

    val selectedContentProperty = property(contentListProperty, selectedContentIndexProperty) {
        selectedContentIndexProperty.value?.let { contentListProperty.value.getOrNull(it) }
    }

    fun selectContent(content: IInfoBarContent) {
        val index = contentListProperty.value.indexOf(content)
        selectedEntryProperty.value?.selectedInfoBarIndexProperty?.value = index
    }
}
