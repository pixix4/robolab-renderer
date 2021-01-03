package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.net.IRobolabServer
import de.robolab.client.ui.dialog.TokenDialog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory> {
    return listOf(LocalFilePlanetLoader)
}

/**
 * Instructs the client to request a new authentication token for the specified server.
 *
 * @param server Server for the token request
 * @param userConfirm Show request confirm dialog to the user
 *
 * @return `true` if the user
 */
actual suspend fun requestAuthToken(
    server: IRobolabServer,
    userConfirm: Boolean
): Boolean {
    val deferred = CompletableDeferred<Boolean>()
    withContext(Dispatchers.Main) {
        TokenDialog.open(server, userConfirm) {
            deferred.complete(it)
        }
    }
    return deferred.await()
}
