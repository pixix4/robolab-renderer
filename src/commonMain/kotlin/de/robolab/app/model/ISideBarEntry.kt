package de.robolab.app.model

import de.westermann.kobserve.ReadOnlyProperty

interface ISideBarEntry {

    val titleProperty: ReadOnlyProperty<String>
    val subtitleProperty: ReadOnlyProperty<String>
    val tabNameProperty: ReadOnlyProperty<String>
    val unsavedChangesProperty: ReadOnlyProperty<Boolean>

    val parent: ISideBarGroup?
}