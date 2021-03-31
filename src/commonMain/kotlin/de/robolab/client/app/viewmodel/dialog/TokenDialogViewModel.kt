package de.robolab.client.app.viewmodel.dialog

import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.net.IRobolabServer

class TokenDialogViewModel(
    val server: IRobolabServer,
    val userConfirm: Boolean,
    val onFinish: (Boolean) -> Unit
) : DialogViewModel("Request access token")
