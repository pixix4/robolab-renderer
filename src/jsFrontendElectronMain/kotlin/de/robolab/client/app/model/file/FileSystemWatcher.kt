package de.robolab.client.app.model.file

import de.robolab.common.externaljs.dynamicOf
import de.robolab.common.externaljs.fs.watch
import de.westermann.kobserve.event.EventHandler

class FileSystemWatcher(baseDirectory: File) {

    val onFolderChange = EventHandler<File>()

    init {
        watch(baseDirectory.absolutePath, dynamicOf(
            "encoding" to "utf8",
            "persistent" to false,
            "recursive" to true,
        )) { _, filename: String ->
            onFolderChange.emit(baseDirectory.resolveChildren(filename))
        }
    }
}
