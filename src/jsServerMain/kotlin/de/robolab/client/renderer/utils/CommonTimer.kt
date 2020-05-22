package de.robolab.client.renderer.utils

import de.westermann.kobserve.event.EventHandler

actual class CommonTimer actual constructor(fps: Double) : ITimer {
    override var fps: Double
        get() = 0.0
        set(value) {
            throw UnsupportedOperationException()
        }
    override val onRender: EventHandler<Double>
        get() = throw UnsupportedOperationException()

    override fun start() {
        throw UnsupportedOperationException()
    }

    override fun stop() {
        throw UnsupportedOperationException()
    }
}