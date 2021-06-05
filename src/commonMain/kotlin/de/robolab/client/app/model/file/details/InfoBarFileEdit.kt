package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPath
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.DelegatePropertyAccessor
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class InfoBarFileEdit(
    private val planetEntry: FilePlanetDocument,
    val uiController: UiController,
) : FilePlanetDocument.FilePlanetSideBarTab<EditPlanetDrawable>(
    "Edit",
    MaterialIcon.CODE
), SideBarContentViewModel {

    override val drawable = EditPlanetDrawable(planetEntry.planetFile, planetEntry.transformationStateProperty)

    override fun importPlanet(planet: Planet) {
        drawable.importPlanet(planet)
    }

    override val parent: SideBarContentViewModel? = null
    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)

    override val topToolBar = buildFormContent {
        button("Transform") {
            transform()
        }
    }
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

            planetEntry.planetFile.parse(value, group)
        }

        override fun get(): String {
            return planetEntry.content
        }

    }, planetEntry.planetFile.planetProperty)
    var content by stringContentProperty

    fun undo() {
        planetEntry.planetFile.planetProperty.undo()
    }

    fun redo() {
        planetEntry.planetFile.planetProperty.redo()
    }

    fun save() {
        GlobalScope.launch {
            planetEntry.filePlanet.save()
        }
    }

    fun transform() {
        planetEntry.transform()
    }

    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetEntry.planetFile)
    val detailBoxProperty: ObservableValue<ViewModel> = drawable.focusedElementsProperty.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> PointDetailBox(first, planetEntry.planetFile)
            is PlanetPath -> PathDetailBox(first, planetEntry.planetFile)
            else -> statisticsDetailBox
        }
    }

    val actionHintList = drawable.view.actionHintList

    sealed class Change {
        data class LineModified(val line: List<Int>) : Change()
        data class LineCountModified(val from: Int, val to: Int) : Change()
    }
}
