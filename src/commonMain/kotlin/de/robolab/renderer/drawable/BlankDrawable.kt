package de.robolab.renderer.drawable

import de.robolab.renderer.drawable.base.GroupDrawable

/**
 * This object does nothing...
 * It is the default drawable of a plotter that only shows the background.
 */
object BlankDrawable : GroupDrawable(GridLinesDrawable, GridNumbersDrawable)
