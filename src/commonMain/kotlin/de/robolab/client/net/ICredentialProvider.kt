package de.robolab.client.net

import de.robolab.common.net.headers.AuthorizationHeader

interface ICredentialProvider {
    val username: String
    val password: String
}

fun ICredentialProvider.toAuthHeader() =
    if (this is AuthorizationHeader.Basic) this else AuthorizationHeader.Basic(username, password)