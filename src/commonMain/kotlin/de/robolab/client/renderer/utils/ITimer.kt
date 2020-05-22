package de.robolab.client.renderer.utils

import de.westermann.kobserve.event.EventHandler

interface ITimer {

    var fps: Double

    val onRender: EventHandler<Double>

    fun start()

    fun stop()

}