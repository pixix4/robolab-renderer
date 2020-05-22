package de.robolab.client.communication

expect fun httpRequest(url: String, onFinish: (String?) -> Unit)
