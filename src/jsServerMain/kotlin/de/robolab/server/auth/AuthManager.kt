package de.robolab.server.auth

import de.robolab.server.net.Client

class AuthManager(val authProviders: Map<String, IAuthProvider>) {
    constructor(authProviders: List<IAuthProvider>) : this(authProviders.associateBy { it::class.simpleName!! })
}

interface IAuthProvider {
    suspend fun auth(client: Client, asAdmin: Boolean): User
}