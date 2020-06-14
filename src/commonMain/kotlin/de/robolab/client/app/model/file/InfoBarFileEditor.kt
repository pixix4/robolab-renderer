package de.robolab.client.app.model.file

import de.robolab.client.app.model.IInfoBarContent
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.common.planet.IPlanetValue
import de.westermann.kobserve.event.EventHandler
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

    fun save() {
        filePlanetEntry.save()
    }

    var ignoreSetLine = false
    fun selectLine(line: Int) {
        val obj = filePlanetEntry.planetFile.lineNumberToValue(line) ?: return
        ignoreSetLine = true
        filePlanetEntry.drawable.focus(obj)
        ignoreSetLine = false
    }

    val onSetLine = EventHandler<Int>()

    private fun findObjForPoint(point: PointAnimatableManager.AttributePoint): IPlanetValue {
        val planet = filePlanetEntry.planetFile.planet

        planet.targetList.find { it.target == point.coordinate }?.let {
            return it
        }
        planet.targetList.find { it.exposure == point.coordinate }?.let {
            return it
        }
        planet.pathSelectList.find { it.point == point.coordinate }?.let {
            return it
        }
        planet.pathList.find { point.coordinate in it.exposure }?.let {
            return it
        }

        return point
    }

    init {
        filePlanetEntry.drawable.focusedElementsProperty.onChange {
            val first = filePlanetEntry.drawable.focusedElementsProperty.value.firstOrNull()
            if (!ignoreSetLine && first != null) {
                val obj = if (first is PointAnimatableManager.AttributePoint) {
                    findObjForPoint(first)
                } else first

                val index = filePlanetEntry.planetFile.valueToLineNumber(obj)
                if (index != null) {
                    onSetLine.emit(index)
                }
            }
        }
    }

    sealed class Change {
        data class LineModified(val line: List<Int>) : Change()
        data class LineCountModified(val from: Int, val to: Int) : Change()
    }
}
