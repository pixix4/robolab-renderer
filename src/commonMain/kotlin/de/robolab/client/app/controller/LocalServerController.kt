package de.robolab.client.app.controller

import de.robolab.client.app.model.file.getFilePlanetLoaderFactoryList
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.property.mapBinding

class LocalServerController() {

    private val loaderFactoryList = getFilePlanetLoaderFactoryList()

    val localPlanetLoaderProperty = PreferenceStorage.remoteFilesProperty.mapBinding { local ->
        loaderFactoryList.asSequence().mapNotNull { it.create(local) }.firstOrNull()
    }
}
