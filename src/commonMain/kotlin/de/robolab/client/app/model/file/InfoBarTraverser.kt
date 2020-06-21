package de.robolab.client.app.model.file

import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.traverser.DefaultTraverser
import de.robolab.common.utils.Logger
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

class InfoBarTraverser(private val filePlanetEntry: FilePlanetEntry) :
    IInfoBarContent {

    private val logger = Logger(this)

    override val nameProperty = constObservable("Traverser")

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
