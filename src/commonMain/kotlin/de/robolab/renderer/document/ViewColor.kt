package de.robolab.renderer.document

import de.robolab.renderer.animation.IInterpolatable
import de.robolab.renderer.data.Color
import de.robolab.theme.IThemePlotter

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
            acc.interpolate(pair.first.toColor(theme), pair.second)
        }
    }

    sealed class C {
        abstract fun toColor(theme: IThemePlotter): Color
        
        object POINT_RED: C() {
            override fun toColor(theme: IThemePlotter): Color {
                return theme.redColor
            }
        }
        object POINT_BLUE: C() {
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
        };

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