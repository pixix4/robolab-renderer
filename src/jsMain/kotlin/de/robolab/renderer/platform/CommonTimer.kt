package de.robolab.renderer.platform

import de.westermann.kobserve.event.EventHandler
import kotlin.browser.window

actual class CommonTimer actual constructor(fps: Double) : ITimer {

    private var currentFps = 0.0
    private var thresholdMs = 0.0
    private val fpsBuffer = DoubleArray(10) { 0.0 }
    private var fpsBufferPointer = 0

    override var fps: Double
        get() = currentFps
        set(value) {
            thresholdMs = 1_000.0 / value
            currentFps = value
            fpsBufferPointer = 0
        }

    private var requestId: Int? = null

    override val onRender = EventHandler<Double>()

    override fun start() {
        if (requestId == null) {
            requestId = window.requestAnimationFrame(this::callback)
        }
    }

    override fun stop() {
        val id = requestId ?: return
        window.cancelAnimationFrame(id)
        requestId = null
    }

    private fun updateBuffer(ms_offset: Double) {
        fpsBuffer[fpsBufferPointer] = ms_offset

        fpsBufferPointer += 1

        if (fpsBufferPointer >= fpsBuffer.size) {
            fpsBufferPointer = 0

            val avg = fpsBuffer.sum() / fpsBuffer.size
            currentFps = 1_000.0 / avg
        }
    }

    private var lastTime = 0.0
    private fun callback(msTime: Double) {
        if (lastTime == 0.0) {
            lastTime = msTime
            onRender.emit(0.0)
        } else {
            val diff = msTime - lastTime
            //if (diff >= thresholdMs) {
                lastTime = msTime

                updateBuffer(diff)
                onRender.emit(diff)
            //}
        }

        requestId = window.requestAnimationFrame(this::callback)
    }

    init {
        this.fps = fps
    }
}
