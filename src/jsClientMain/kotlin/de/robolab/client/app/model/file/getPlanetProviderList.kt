package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.FilePlanetProvider
import de.robolab.client.app.model.file.provider.LocalWebPlanetLoader
import de.robolab.client.app.model.file.provider.RemoteFilePlanetLoader
import de.robolab.client.net.RESTRobolabServer
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.list.observableListOf
import kotlin.browser.window

actual fun getPlanetProviderList(): ObservableList<FilePlanetProvider<*>> {
    return observableListOf(
        FilePlanetProvider(LocalWebPlanetLoader()),
        FilePlanetProvider(
            RemoteFilePlanetLoader(
                RESTRobolabServer(
                    window.location.hostname,
                    0,
                    window.location.protocol.replace(":", "")
                )
            )
        )
    )
}