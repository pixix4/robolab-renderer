package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.auth.DeviceAuthPrompt
import de.robolab.client.net.requests.auth.DeviceAuthPromptCallbacks

actual fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory> {
    TODO("Not yet implemented")
}

actual fun handleAuthPrompt(
    prompt: DeviceAuthPrompt
): DeviceAuthPromptCallbacks {
    TODO("Not yet implemented")
}
