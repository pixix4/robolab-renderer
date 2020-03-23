package de.robolab.renderer.platform

import de.westermann.kobserve.event.EventHandler

interface ITimer {

    var fps: Double

    val onRender: EventHandler<Double>

    fun start()

    fun stop()

}