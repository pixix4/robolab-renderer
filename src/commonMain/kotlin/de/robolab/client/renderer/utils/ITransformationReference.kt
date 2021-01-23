package de.robolab.client.renderer.utils

interface ITransformationReference {

    val transformation: ITransformation?

    var autoCentering: Boolean
    fun centerPlanet(forceAutoScale: Boolean = false, duration: Double = 0.0)
}