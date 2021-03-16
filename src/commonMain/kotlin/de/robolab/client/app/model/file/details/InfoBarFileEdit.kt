package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.common.planet.IPlanetValue
import de.robolab.common.planet.Path
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class InfoBarFileEdit(
    private val planetEntry: FilePlanetDocument,
    val uiController: UiController,
)  : FilePlanetDocument.FilePlanetSideBarTab<EditPlanetDrawable>(
    "Edit",
    MaterialIcon.CODE
), SideBarContentViewModel {

    override val drawable = EditPlanetDrawable(planetEntry.planetFile, planetEntry.transformationStateProperty)

    override fun importPlanet(planet: Planet) {
        drawable.importPlanet(planet)
    }

    override val parent: SideBarContentViewModel? = null
    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)

    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent { }

    private var lastChange: Change = Change.LineCountModified(-1, 0)

    val stringContentProperty = property(object : DelegatePropertyAccessor<String> {
        override fun set(value: String) {
            val lastLines = planetEntry.content.split('\n')
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
                planetEntry.planetFile.replaceContent(value.split('\n'))
            } else {
                planetEntry.planetFile.content = value.split('\n')
            }
        }

        override fun get(): String {
            return planetEntry.content
        }

    }, planetEntry.planetFile.history)
    var content by stringContentProperty

    fun undo() {
        planetEntry.planetFile.history.undo()
    }

    fun redo() {
        planetEntry.planetFile.history.redo()
    }

    fun save() {
        GlobalScope.launch {
            planetEntry.filePlanet.save()
        }
    }

    fun transform() {
        planetEntry.transform()
    }

    fun format() {
        planetEntry.format(false)
    }
    fun formatExplicit() {
        planetEntry.format(true)
    }

    var ignoreSetLine = false
    fun selectLine(line: Int) {
        val obj = planetEntry.planetFile.lineNumberToValue(line) ?: return
        ignoreSetLine = true
        drawable.focus(obj)
        ignoreSetLine = false
    }

    val onSetLine = EventHandler<Int>()

    private fun findObjForPoint(point: PointAnimatableManager.AttributePoint): IPlanetValue {
        val planet = planetEntry.planetFile.planet

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

    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetEntry.planetFile)
    val detailBoxProperty: ObservableValue<Any> = drawable.focusedElementsProperty.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> PointDetailBox(first, planetEntry.planetFile)
            is Path -> PathDetailBox(first, planetEntry.planetFile)
            else -> statisticsDetailBox
        }
    }

    init {
        drawable.focusedElementsProperty.onChange {
            val first = drawable.focusedElementsProperty.value.firstOrNull()
            if (!ignoreSetLine && first != null) {
                val obj = if (first is PointAnimatableManager.AttributePoint) {
                    findObjForPoint(first)
                } else first

                val index = planetEntry.planetFile.valueToLineNumber(obj)
                if (index != null) {
                    onSetLine.emit(index)
                }
            }
        }
    }

    val actionHintList = drawable.view.actionHintList

    sealed class Change {
        data class LineModified(val line: List<Int>) : Change()
        data class LineCountModified(val from: Int, val to: Int) : Change()
    }
}
