package de.robolab.client.app.model.file

import de.robolab.client.app.model.IInfoBarContent
import de.westermann.kobserve.property.DelegatePropertyAccessor
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

class InfoBarFileEditor(private val filePlanetEntry: FilePlanetEntry) : IInfoBarContent {

    override val nameProperty = constObservable("Editor")


    private var lastChange: Change = Change.LineCountModified(-1, 0)

    val contentProperty = property(object : DelegatePropertyAccessor<String> {
        override fun set(value: String) {
            val lastLines = filePlanetEntry.content.split('\n')
            val lines = value.split('\n')

            if (lastLines == lines) return

            val change = if (lastLines.size != lines.size) {
                Change.LineCountModified(lastLines.size, lines.size)
            } else {
                val changedIndex = lastLines.zip(lines)
                    .withIndex()
                    .filter { (_, v) -> v.first != v.second }
                    .map { it.index }

                Change.LineModified(changedIndex)
            }

            val group = lastChange == change
            lastChange = change

            if (group) {
                filePlanetEntry.planetFile.replaceContent(value)
            } else {
                filePlanetEntry.planetFile.content = value
            }
        }

        override fun get(): String {
            return filePlanetEntry.content
        }

    }, filePlanetEntry.planetFile.planetProperty)
    var content by contentProperty

    fun undo() {
        filePlanetEntry.planetFile.history.undo()
    }

    fun redo() {
        filePlanetEntry.planetFile.history.redo()
    }

    sealed class Change {
        data class LineModified(val line: List<Int>) : Change()
        data class LineCountModified(val from: Int, val to: Int) : Change()
    }
}
