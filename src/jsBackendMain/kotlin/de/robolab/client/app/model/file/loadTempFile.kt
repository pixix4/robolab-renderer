package de.robolab.client.app.model.file

actual fun loadTempFile(file: String): List<String>? {
    return null
}

actual fun saveTempFile(file: String, content: List<String>) {
    throw NotImplementedError()
}
