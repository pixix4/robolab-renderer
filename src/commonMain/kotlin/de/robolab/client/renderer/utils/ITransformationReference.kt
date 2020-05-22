package de.robolab.client.renderer.utils

interface ITransformationReference {

    val transformation: Transformation?

    var autoCentering: Boolean
    fun centerPlanet(duration: Double = 0.0)
}