package de.robolab.communication

expect fun httpRequest(url: String, onFinish: (String?) -> Unit)
