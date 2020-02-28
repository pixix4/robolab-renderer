package de.robolab.jfx

import javafx.scene.canvas.Canvas

/**
 * @author lars
 */
class ResizeableCanvas : Canvas() {

    override fun isResizable() = true

    private val drawHooks = mutableListOf<() -> Unit>()

    init {
        widthProperty().addListener { _ -> draw() }
        heightProperty().addListener { _ -> draw() }
    }

    override fun prefWidth(height: Double) = width

    override fun prefHeight(width: Double) = height

    fun draw() = drawHooks.forEach { it() }

    fun addDrawHook(drawHook: () -> Unit) = drawHooks.add(drawHook)
}