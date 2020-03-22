package de.robolab.renderer.platform

import de.westermann.kobserve.event.EventHandler
import javafx.animation.AnimationTimer

actual class CommonTimer actual constructor(fps: Double): ITimer {

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

    private val timer: AnimationTimer

    override val onRender = EventHandler<Double>()

    override fun start() {
        timer.start()
    }

    override fun stop() {
        timer.stop()
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

    init {
        this.fps = fps

        timer = object : AnimationTimer() {

            private var lastTime = 0.0

            override fun handle(nanoTime: Long) {
                val msTime = nanoTime / 1_000_000.0

                if (lastTime == 0.0) {
                    lastTime = msTime
                    onRender.emit(0.0)
                    return
                }

                val diff = msTime - lastTime
                if (diff >= thresholdMs) {
                    lastTime = msTime

                    updateBuffer(diff)
                    onRender.emit(diff)
                }
            }
        }
    }
}
