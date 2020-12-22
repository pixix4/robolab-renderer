package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.net.IRobolabServer

actual fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory> {
    TODO("Not yet implemented")
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
    TODO("Not yet implemented")
}
