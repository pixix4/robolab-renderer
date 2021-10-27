package de.robolab.client.app.model.file

import de.robolab.client.app.controller.DialogController
import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.app.viewmodel.dialog.TokenDialogViewModel
import de.robolab.client.net.requests.auth.DeviceAuthPrompt
import de.robolab.client.net.requests.auth.IDeviceAuthPromptCallbacks
import de.westermann.kobserve.property.property
import kotlin.time.DurationUnit
import kotlin.time.toDuration

actual fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory> {
    return listOf()
}

actual fun handleAuthPrompt(
    prompt: DeviceAuthPrompt
): IDeviceAuthPromptCallbacks {
    val deviceAuthPrompt = property(prompt)

    val viewModel = TokenDialogViewModel(deviceAuthPrompt)

    val callbacks = object : IDeviceAuthPromptCallbacks {
        override fun onPromptSuccess() {
            viewModel.close()
        }

        override fun onPromptError() {
            viewModel.close()
        }

        override fun onPromptRefresh(newPrompt: DeviceAuthPrompt) {
            deviceAuthPrompt.value = newPrompt
        }
    }

    DialogController.open(viewModel)

    return callbacks
}
