package de.robolab.client.net.requests.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class DeviceAuthResponse(
    @SerialName("device_code")
    val deviceCode: String,
    @SerialName("user_code")
    val userCode: String,
    @SerialName("verification_uri")
    val verificationURI: String,
    @SerialName("interval")
    val interval: Int?,
    @SerialName("expires_in")
    val expiresIn: Long?
) {
    @Transient
    val prompt: DeviceAuthPrompt
        get() = DeviceAuthPrompt(userCode, verificationURI, expiresIn)
}

data class DeviceAuthPrompt(
    val userCode: String,
    val verificationURI: String,
    val expiresIn: Long?
)

interface IDeviceAuthPromptCallbacks {
    fun onPromptSuccess()
    fun onPromptError()
    fun onPromptRefresh(newPrompt: DeviceAuthPrompt)
}
