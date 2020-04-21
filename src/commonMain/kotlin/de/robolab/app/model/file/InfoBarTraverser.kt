package de.robolab.app.model.file

import de.robolab.app.controller.TraverserBarController
import de.robolab.app.model.IInfoBarContent
import de.robolab.traverser.DefaultTraverser
import de.robolab.traverser.ITraverserState
import de.robolab.traverser.TraverserState
import de.robolab.utils.Logger
import de.westermann.kobserve.Property
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.property

class InfoBarTraverser(private val filePlanetEntry: FilePlanetEntry) : IInfoBarContent {

    private val logger = Logger(this)

    override val nameProperty = constProperty("Traverser")

    val traverserProperty = property<TraverserBarController?>(null)

    fun traverse() {
        val planet = filePlanetEntry.planetFile.planet

        logger.info { "Starting new traversal of '${planet.name}'" }
        try {
            traverserProperty.value = TraverserBarController(DefaultTraverser(planet, true))
                    // .filter { it.status != ITraverserState.Status.Running }
                    // .forEach {
                    //     logger.info { it.getTrail() }
                    // }
        } catch (e: Exception) {
            logger.error { e }
        }
    }
}
