package de.robolab.renderer.interaction

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.platform.*

class CompositionInteraction(
        private var layers: List<ICanvasListener>
) : ICanvasListener {

    constructor(vararg layers: ICanvasListener) : this(layers.toList())

    private fun push(layer: ICanvasListener) {
        layers = listOf(layer) + layers
    }

    private fun pop(): ICanvasListener {
        val layer = layers.first()
        layers = layers.drop(1)
        return layer
    }

    override fun onUpdate(ms_offset: Double): Boolean {
        var changes = false

        for (layer in layers) {
            if (layer.onUpdate(ms_offset)) {
                changes = true
            }
        }

        return changes
    }

    override fun onMouseDown(event: MouseEvent): Boolean {
        for (layer in layers) {
            if (layer.onMouseDown(event)) {
                return true
            }
        }

        return false
    }

    override fun onMouseUp(event: MouseEvent): Boolean {
        for (layer in layers) {
            if (layer.onMouseUp(event)) {
                return true
            }
        }

        return false
    }

    override fun onMouseMove(event: MouseEvent): Boolean {
        for (layer in layers) {
            if (layer.onMouseMove(event)) {
                return true
            }
        }

        return false
    }

    override fun onMouseDrag(event: MouseEvent): Boolean {
        for (layer in layers) {
            if (layer.onMouseDrag(event)) {
                return true
            }
        }

        return false
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
        for (layer in layers) {
            if (layer.onMouseClick(event)) {
                return true
            }
        }

        return false
    }

    override fun onScroll(event: ScrollEvent): Boolean {
        for (layer in layers) {
            if (layer.onScroll(event)) {
                return true
            }
        }

        return false
    }

    override fun onZoom(event: ZoomEvent): Boolean {
        for (layer in layers) {
            if (layer.onZoom(event)) {
                return true
            }
        }

        return false
    }

    override fun onRotate(event: RotateEvent): Boolean {
        for (layer in layers) {
            if (layer.onRotate(event)) {
                return true
            }
        }

        return false
    }

    override fun onResize(size: Dimension): Boolean {
        for (layer in layers) {
            if (layer.onResize(size)) {
                return true
            }
        }

        return false
    }
}