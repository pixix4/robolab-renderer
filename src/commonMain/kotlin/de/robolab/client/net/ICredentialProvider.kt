package de.robolab.client.net

interface ICredentialProvider {
    fun copyUsername(target: CharArray)
    fun copyPassword(target: CharArray)
}