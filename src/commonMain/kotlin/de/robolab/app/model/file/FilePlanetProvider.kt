package de.robolab.app.model.file

import de.robolab.app.model.IProvider

expect class FilePlanetProvider() : IProvider {
    fun loadEntry(entry: FilePlanetEntry, onFinish: (String?) -> Unit)

    fun saveEntry(entry: FilePlanetEntry, onFinish: (Boolean) -> Unit)
}
