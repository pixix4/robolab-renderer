package de.robolab.client.app.model.file

import de.robolab.client.app.controller.DialogController
import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.app.viewmodel.dialog.TokenDialogViewModel
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.auth.DeviceAuthPrompt
import de.robolab.client.net.requests.auth.DeviceAuthPromptCallbacks
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

actual fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory> {
    return listOf(LocalFilePlanetLoader)
}

actual fun handleAuthPrompt(
    prompt: DeviceAuthPrompt
): DeviceAuthPromptCallbacks {
    //TODO: Handle Auth Prompt in FrontendElectron
    if(prompt.expiresIn != null){
        println("New Auth-Prompt (${prompt.userCode}), please visit ${prompt.verificationURI} within ${prompt.expiresIn.toDuration(
            DurationUnit.SECONDS)}")
    }else{
        println("New Auth-Prompt (${prompt.userCode}), please visit ${prompt.verificationURI}")
    }
    return object : DeviceAuthPromptCallbacks{
        override fun onPromptError() {
            println("Auth-Prompt-Error!")
        }

        override fun onPromptRefresh(newPrompt: DeviceAuthPrompt) {
            if(prompt.expiresIn != null){
                println("Auth-Prompt-Refresh (${prompt.userCode}), please visit ${prompt.verificationURI} within ${prompt.expiresIn.toDuration(
                    DurationUnit.SECONDS)}")
            }else{
                println("Auth-Prompt-Refresh (${prompt.userCode}), please visit ${prompt.verificationURI}")
            }
        }

        override fun onPromptSuccess() {
            println("Auth-Prompt-Success!")
        }
    }
    /*
    val deferred = CompletableDeferred<Boolean>()
    withContext(Dispatchers.Main) {
        DialogController.open(
            TokenDialogViewModel(server, userConfirm) {
                deferred.complete(it)
            }
        )
    }
    return deferred.await()
    */
}
