package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.net.IRobolabServer

object LoadRemoteExamStateEvent

expect fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory>

/**
 * Instructs the client to request a new authentication token for the specified server.
 *
 * @param server Server for the token request
 * @param userConfirm Show request confirm dialog to the user
 *
 * @return `true` if the user
 */
expect suspend fun requestAuthToken(server: IRobolabServer, userConfirm: Boolean): Boolean
