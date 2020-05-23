package de.robolab.client.app.model.file

import de.robolab.client.app.model.IProvider
import de.robolab.client.app.model.ISideBarEntry
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableProperty

actual class FilePlanetProvider actual constructor() : IProvider {
    actual fun loadEntry(entry: FilePlanetEntry, onFinish: (String?) -> Unit) {
        throw UnsupportedOperationException()
    }

    actual fun saveEntry(entry: FilePlanetEntry, onFinish: (Boolean) -> Unit) {
        throw UnsupportedOperationException()
    }

    actual val planetList: ObservableMutableList<FilePlanetEntry>
        get() = throw UnsupportedOperationException()

    override val searchStringProperty: ObservableProperty<String>
        get() = throw UnsupportedOperationException()

    override val entryList: ObservableList<ISideBarEntry>
        get() = throw UnsupportedOperationException()
}