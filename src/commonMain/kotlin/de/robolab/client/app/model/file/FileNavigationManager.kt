package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.app.viewmodel.MainViewModel
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.auth.DeviceAuthPrompt
import de.robolab.client.net.requests.auth.DeviceAuthPromptCallbacks

object LoadRemoteExamStateEvent

expect fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory>

expect fun handleAuthPrompt(prompt: DeviceAuthPrompt): DeviceAuthPromptCallbacks