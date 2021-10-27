package de.robolab.client.net.requests.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DeviceAuthResponse(
    @SerialName("device_code")
    val deviceCode: String,
    @SerialName("user_code")
    val userCode: String,
    @SerialName("verification_uri")
    val verificationURI: String,
    @SerialName("verification_uri_complete")
    val verificationURIComplete: String? = null,
    @SerialName("interval")
    val interval: Int? = null,
    @SerialName("expires_in")
    val expiresIn: Long? = null
) {
    val prompt: DeviceAuthPrompt
        get() = DeviceAuthPrompt(userCode, verificationURI, verificationURIComplete, expiresIn)
}

data class DeviceAuthPrompt(
    val userCode: String,
    val verificationURI: String,
    val verificationURIComplete: String? = null,
    val expiresIn: Long? = null,
)

interface IDeviceAuthPromptCallbacks {
    fun onPromptSuccess()
    fun onPromptError()
    fun onPromptRefresh(newPrompt: DeviceAuthPrompt)
}
