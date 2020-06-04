package de.robolab.client.app.model.file

import de.robolab.client.app.model.IProvider
import de.robolab.client.app.model.INavigationBarEntry
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableProperty

actual class FilePlanetProvider actual constructor() : IProvider {
    actual suspend fun loadEntry(entry: FilePlanetEntry): String? {
        throw UnsupportedOperationException()
    }

    actual suspend fun saveEntry(entry: FilePlanetEntry): Boolean {
        throw UnsupportedOperationException()
    }

    actual val planetList: ObservableMutableList<FilePlanetEntry>
        get() = throw UnsupportedOperationException()

    override val searchStringProperty: ObservableProperty<String>
        get() = throw UnsupportedOperationException()

    override val entryList: ObservableList<INavigationBarEntry>
        get() = throw UnsupportedOperationException()
}