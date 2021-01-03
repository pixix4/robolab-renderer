package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.traverser.DefaultTraverser
import de.robolab.common.utils.Logger
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property

class InfoBarFileTraverse(private val planetDocument: FilePlanetDocument) :
    IInfoBarContent {

    private val logger = Logger(this)

    val traverserProperty = property<TraverserBarController?>(null)

    val traverserRenderStateProperty  = traverserProperty
        .nullableFlatMapBinding { it?.renderState }

    fun traverse() {
        val planet = planetDocument.planetFile.planet

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
