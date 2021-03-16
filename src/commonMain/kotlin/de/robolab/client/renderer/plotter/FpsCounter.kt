package de.robolab.client.renderer.plotter

import kotlin.math.roundToInt

class FpsCounter {

    private val fpsWindow = DoubleArray(FPS_WINDOW_SIZE) { 0.0 }
    private var _fpsDouble = 0.0
    private var _fpsInt = 0
    private var index = 0

    val fpsDouble
        get() = _fpsDouble
    val fpsInt
        get() = _fpsInt

    fun update(msOffset: Double) {
        if (msOffset > 0.0) {
            val oldFps = fpsWindow[index]
            val newFps = 1000.0 / FPS_WINDOW_SIZE / msOffset
            fpsWindow[index++] = newFps
            index %= FPS_WINDOW_SIZE

            _fpsDouble = _fpsDouble - oldFps + newFps
            _fpsInt = _fpsDouble.roundToInt()
        }
    }

    companion object {
        private const val FPS_WINDOW_SIZE: Int = 60
    }
}
