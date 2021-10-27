package de.robolab.client.app.viewmodel.dialog

import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.auth.DeviceAuthPrompt
import de.westermann.kobserve.base.ObservableValue

class TokenDialogViewModel(
    val deviceAuthPrompt: ObservableValue<DeviceAuthPrompt>
) : DialogViewModel("Request access token")
