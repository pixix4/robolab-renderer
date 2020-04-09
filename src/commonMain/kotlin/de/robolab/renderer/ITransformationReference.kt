package de.robolab.renderer

import de.robolab.renderer.utils.Transformation

interface ITransformationReference {

    val transformation: Transformation?

    var autoCentering: Boolean
    fun centerPlanet(duration: Double = 0.0)
}