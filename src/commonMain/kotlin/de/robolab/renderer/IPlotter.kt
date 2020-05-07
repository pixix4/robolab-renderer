package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.utils.Pointer
import de.robolab.renderer.utils.Transformation
import de.westermann.kobserve.base.ObservableProperty

interface IPlotter {
    val context: DrawContext

    fun render(ms_offset: Double)

    val pointerProperty: ObservableProperty<Pointer?>
    val pointer: Pointer?
    fun updatePointer(mousePosition: Point? = pointer?.mousePosition): Point?

    val size: Dimension
    val transformation: Transformation

    val animationTime: Double
}
