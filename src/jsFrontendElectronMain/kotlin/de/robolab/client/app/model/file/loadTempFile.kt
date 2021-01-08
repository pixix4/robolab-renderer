package de.robolab.client.app.model.file

import de.robolab.common.externaljs.fs.existsSync
import de.robolab.common.externaljs.fs.readFileSync
import de.robolab.common.externaljs.fs.writeFileSync

@Suppress("RedundantNullableReturnType")
actual fun loadTempFile(file: String): List<String>? {
    if (existsSync(file)) {
        return readFileSync(file, js("{}")).toString().split("\n")
    }

    return null
}

actual fun saveTempFile(file: String, content: List<String>) {
    if (existsSync(file)) {
        writeFileSync(file, content.joinToString("\n"))
    }
}
