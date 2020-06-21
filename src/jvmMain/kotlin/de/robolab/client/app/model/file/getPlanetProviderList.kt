package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.FilePlanetProvider
import de.robolab.client.app.model.file.provider.FileSystemPlanetLoader
import de.robolab.client.app.model.file.provider.RemoteFilePlanetLoader
import de.robolab.client.net.RESTRobolabServer
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.list.observableListOf
import java.io.File

actual fun getPlanetProviderList(): ObservableList<FilePlanetProvider<*>> {
    return observableListOf(
        FilePlanetProvider(
            RemoteFilePlanetLoader(
                RESTRobolabServer("robolab.pixix4.com", 0, secure = true)
            )
        ),
        FilePlanetProvider(FileSystemPlanetLoader(File("planet")))
    )
}
