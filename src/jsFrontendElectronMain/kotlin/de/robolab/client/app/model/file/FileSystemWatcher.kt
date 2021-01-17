package de.robolab.client.app.model.file

import de.westermann.kobserve.event.EventHandler

class FileSystemWatcher(baseDirectory: File) {

    val onFolderChange = EventHandler<File>()

    init {
        fs.watch(baseDirectory.absolutePath, object : fs.`T$48` {
            override var encoding: String = "utf8"
            override var persistent: Boolean? = false
            override var recursive: Boolean? = true
        }) { _, filename: String ->
            onFolderChange.emit(File(filename))
        }
    }
}
