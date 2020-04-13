package de.robolab.app.model.file

import de.robolab.app.model.IInfoBarContent
import de.robolab.traverser.DefaultTraverser
import de.robolab.traverser.ITraverserState
import de.robolab.traverser.TraverserState
import de.westermann.kobserve.property.constProperty

class InfoBarTraverser(private val filePlanetEntry: FilePlanetEntry) : IInfoBarContent {

    override val nameProperty = constProperty("Traverser")
    
    fun traverse() {
        val planet = filePlanetEntry.planetFile.planet

        println("Starting new traversal of '${planet.name}'")
        try {
            DefaultTraverser(planet, true)
                    .filter { it.status != ITraverserState.Status.Running }
                    .forEach {
                        println(it.getTrail())
                    }
        } catch (e: Exception) {
            println(e)
        }
    }
}
