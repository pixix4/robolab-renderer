package de.robolab.app.controller

import de.robolab.app.model.IPlottable
import de.robolab.app.model.file.FilePlanetProvider
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class SideBarController(
        val selectedEntryProperty: Property<IPlottable?>
) {

    private val filePlanetProvider = FilePlanetProvider()

    val tabProperty = PreferenceStorage.selectedSideBarTab

    val entryListProperty = tabProperty.mapBinding {
        when (tabProperty.value) {
            Tab.GROUP -> observableListOf()
            Tab.PLANET -> observableListOf()
            Tab.FILE -> filePlanetProvider.sortedPlanetList
        }
    }

    enum class Tab(val label: String) {
        GROUP("Groups"),
        PLANET("Planets"),
        FILE("Files")
    }
}
