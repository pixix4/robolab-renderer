package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.utils.Pointer
import de.robolab.renderer.utils.Transformation
import de.westermann.kobserve.Property

interface IPlotter {
    fun render(ms_offset: Double)

    val pointerProperty: Property<Pointer?>
    val pointer: Pointer?
    fun updatePointer(mousePosition: Point? = pointer?.mousePosition): Point?

    val size: Dimension
    val transformation: Transformation

    val animationTime: Double
}