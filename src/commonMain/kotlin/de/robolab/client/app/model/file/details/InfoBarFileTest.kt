package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.testing.TestTraversalController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.traverser.DefaultTraverser
import de.robolab.common.planet.LookupPlanet
import de.robolab.common.planet.Planet
import de.robolab.common.testing.testWith
import de.robolab.common.utils.Logger
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.*
import de.westermann.kobserve.toggle

class InfoBarFileTest(
    private val planetEntry: FilePlanetDocument,
    val uiController: UiController,
) : FilePlanetDocument.FilePlanetSideBarTab<SimplePlanetDrawable>(
    "Test suite",
    MaterialIcon.BUG_REPORT,
), SideBarContentViewModel {

    val testProperty = property<TestTraversalController?>(null)

    override val drawable = SimplePlanetDrawable(planetEntry.transformationStateProperty)

    override fun importPlanet(planet: Planet) {
        drawable.importPlanet(planet)
    }

    override val parent: SideBarContentViewModel? = null
    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)

    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent { }

    private val logger = Logger(this)

    val titleProperty = testProperty.nullableFlatMapBinding { it?.title }.mapBinding { it ?: "" }
    val goalFilters = testProperty.mapBinding { it?.goalFilters ?: observableListOf() }
    val statusFilters = testProperty.mapBinding { it?.statusFilters ?: observableListOf() }
    val stickToTableBottom = testProperty.nullableFlatMapBinding { it?.stickToTableBottom }.mapBinding { it ?: false }
    val currentTestRuns = testProperty.mapBinding { it?.currentTestRuns ?: observableListOf() }

    fun test() {
        val planet = planetEntry.planetFile.planet

        logger.info { "Starting new test traversal of '${planet.name}'" }
        try {
            val traverser = DefaultTraverser(planet, true)
            testProperty.value = TestTraversalController(
                LookupPlanet(planet).testWith(traverser)
            )
        } catch (e: Exception) {
            logger.error { e }
        }
    }
}
