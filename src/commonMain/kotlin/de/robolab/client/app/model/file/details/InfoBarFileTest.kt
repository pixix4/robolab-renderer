package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.testing.TestTraversalController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.traverser.DefaultTraverser
import de.robolab.common.planet.LookupPlanet
import de.robolab.common.testing.testWith
import de.robolab.common.utils.Logger
import de.westermann.kobserve.property.property

class InfoBarFileTest(private val planetDocument: FilePlanetDocument, testDrawable: SimplePlanetDrawable) :
    IInfoBarContent {

    private val logger = Logger(this)

    val testProperty = property<TestTraversalController?>(null)

    fun test() {
        val planet = planetDocument.planetFile.planet

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
