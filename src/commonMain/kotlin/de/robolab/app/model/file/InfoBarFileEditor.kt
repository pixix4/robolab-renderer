package de.robolab.app.model.file

import de.robolab.app.model.IInfoBarContent
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.property

class InfoBarFileEditor(private val filePlanetEntry: FilePlanetEntry) : IInfoBarContent {

    override val nameProperty = constProperty("Editor")


    private var lastChange: Change = Change.LineCountModified(-1, 0)

    val contentProperty = property(object : FunctionAccessor<String> {
        override fun set(value: String): Boolean {
            val lastLines = filePlanetEntry.content.split('\n')
            val lines = value.split('\n')

            if (lastLines == lines) return true

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
            return true
        }

        override fun get(): String {
            return filePlanetEntry.content
        }

    }, filePlanetEntry.planetFile.planetProperty)
    var content by contentProperty

    sealed class Change {
        data class LineModified(val line: List<Int>) : Change()
        data class LineCountModified(val from: Int, val to: Int) : Change()
    }
}
