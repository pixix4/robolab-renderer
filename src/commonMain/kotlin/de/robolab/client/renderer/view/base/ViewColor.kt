package de.robolab.client.renderer.view.base

import de.robolab.client.renderer.transition.IInterpolatable
import de.robolab.client.theme.IThemePlotter
import de.robolab.common.utils.Color

data class ViewColor(
    val color: C,
    val interpolations: List<Pair<ViewColor, Double>> = emptyList()
) : IInterpolatable<ViewColor> {

    override fun interpolate(toValue: ViewColor, progress: Double): ViewColor {
        return copy(interpolations = interpolations + (toValue to progress))
    }

    override fun interpolateToNull(progress: Double): ViewColor {
        return this
    }

    fun toColor(theme: IThemePlotter): Color {
        return interpolations.fold(color.toColor(theme)) { acc, pair ->
            val toColor = pair.first.toColor(theme)
            val progress = pair.second

            when {
                acc == Color.TRANSPARENT -> toColor.a(progress)
                toColor == Color.TRANSPARENT -> acc.a(1.0 - progress)
                else -> acc.interpolate(toColor, progress)
            }
        }
    }

    sealed class C {
        abstract fun toColor(theme: IThemePlotter): Color

        override fun toString(): String {
            return "C(${this::class.simpleName})"
        }

        object POINT_RED : C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.redColor
            }
        }

        object POINT_BLUE : C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.blueColor
            }
        }
        object PRIMARY_BACKGROUND_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.primaryBackgroundColor
            }
        }
        object SECONDARY_BACKGROUND_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.secondaryBackgroundColor
            }
        }
        object LINE_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.lineColor
            }
        }
        object GRID_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.gridColor
            }
        }
        object GRID_TEXT_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.gridTextColor
            }
        }
        object HIGHLIGHT_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.highlightColor
            }
        }
        object EDIT_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.editColor
            }
        }
        object ROBOT_MAIN_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.robotMainColor
            }
        }
        object ROBOT_DISPLAY_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.robotDisplayColor
            }
        }
        object ROBOT_WHEEL_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.robotWheelColor
            }
        }
        object ROBOT_SENSOR_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.robotSensorColor
            }
        }
        object ROBOT_BUTTON_COLOR: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.robotButtonColor
            }
        }
        class CUSTOM(private val color: Color): C() {
            override fun toColor(theme: IThemePlotter): Color {
                return color
            }

            override fun toString(): String {
                return "C($color)"
            }
        }
    }

    companion object {
        val POINT_RED = ViewColor(C.POINT_RED)
        val POINT_BLUE = ViewColor(C.POINT_BLUE)

        val PRIMARY_BACKGROUND_COLOR = ViewColor(C.PRIMARY_BACKGROUND_COLOR)
        val SECONDARY_BACKGROUND_COLOR = ViewColor(C.SECONDARY_BACKGROUND_COLOR)
        val LINE_COLOR = ViewColor(C.LINE_COLOR)
        val GRID_COLOR = ViewColor(C.GRID_COLOR)
        val GRID_TEXT_COLOR = ViewColor(C.GRID_TEXT_COLOR)
        val HIGHLIGHT_COLOR = ViewColor(C.HIGHLIGHT_COLOR)
        val EDIT_COLOR = ViewColor(C.EDIT_COLOR)
        val ROBOT_MAIN_COLOR = ViewColor(C.ROBOT_MAIN_COLOR)
        val ROBOT_DISPLAY_COLOR = ViewColor(C.ROBOT_DISPLAY_COLOR)
        val ROBOT_WHEEL_COLOR = ViewColor(C.ROBOT_WHEEL_COLOR)
        val ROBOT_SENSOR_COLOR = ViewColor(C.ROBOT_SENSOR_COLOR)
        val ROBOT_BUTTON_COLOR = ViewColor(C.ROBOT_BUTTON_COLOR)
        
        val TRANSPARENT = ViewColor(C.CUSTOM(Color.TRANSPARENT))
        
        fun c(color: Color) = ViewColor(C.CUSTOM(color))
    }
}
