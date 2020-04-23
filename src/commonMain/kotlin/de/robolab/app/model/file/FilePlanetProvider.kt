package de.robolab.app.model.file

import de.robolab.app.model.IProvider
import de.westermann.kobserve.list.ObservableList

expect class FilePlanetProvider() : IProvider {
    fun loadEntry(entry: FilePlanetEntry, onFinish: (String?) -> Unit)

    fun saveEntry(entry: FilePlanetEntry, onFinish: (Boolean) -> Unit)

    val planetList: ObservableList<FilePlanetEntry>
}

fun FilePlanetProvider.findByName(name: String): FilePlanetEntry? {
    if (name.isEmpty()) return null

    return planetList.filter { it.titleProperty.value.contains(name, true) }.minBy { it.titleProperty.value.length }
}
