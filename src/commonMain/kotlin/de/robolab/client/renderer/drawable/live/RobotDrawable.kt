package de.robolab.client.renderer.drawable.live

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.utils.BSpline
import de.robolab.client.renderer.drawable.utils.shift
import de.robolab.client.renderer.transition.IInterpolatable
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.RobotView
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Point
import kotlin.math.PI
import kotlin.math.atan2

class RobotDrawable {

    val view = RobotView(
        DrawRobot(Point.ZERO, 0.0, null),
        null,
        ViewColor.TRANSPARENT
    )

    data class Robot(
        val point: Coordinate,
        val direction: Direction,
        val beforePoint: Boolean,
        val groupNumber: Int?
    ) {
        val afterPoint: Boolean = !beforePoint
    }

    private var planet: Planet? = null

    inner class DrawRobot(
        val position: Point,
        val orientation: Double,
        private val robot: Robot?
    ) : IInterpolatable<DrawRobot> {

        override fun interpolate(toValue: DrawRobot, progress: Double): DrawRobot {
            val orientationDelta = when {
                orientation - toValue.orientation > PI -> toValue.orientation - (orientation - 2 * PI)
                toValue.orientation - orientation > PI -> (toValue.orientation - 2 * PI) - orientation
                else -> toValue.orientation - orientation
            }

            if (robot != null && toValue.robot != null) {
                if (robot.beforePoint && toValue.robot.afterPoint) {
                    return DrawRobot(
                        position.interpolate(toValue.position, progress),
                        orientation + orientationDelta * progress,
                        null
                    )
                }

                var path = Path(
                    robot.point,
                    robot.direction,
                    toValue.robot.point,
                    toValue.robot.direction,
                    1,
                    emptySet(),
                    emptyList(),
                    false
                )
                val planetPath = planet?.pathList?.find { it.equalPath(path) }
                if (planetPath != null) {
                    path = if (path.source == planetPath.source && path.sourceDirection == planetPath.sourceDirection) {
                        path.copy(
                            controlPoints = planetPath.controlPoints
                        )
                    } else {
                        path.copy(
                            controlPoints = planetPath.controlPoints.reversed()
                        )
                    }
                }

                val points = PathAnimatable.getControlPointsFromPath(path)
                val position = BSpline.eval(progress, points)
                val d = BSpline.evalGradient(progress, points)

                return DrawRobot(
                    position,
                    d.toAngle(),
                    null
                )
            }

            return DrawRobot(
                position.interpolate(toValue.position, progress),
                orientation + orientationDelta * progress,
                null
            )
        }

        override fun interpolateToNull(progress: Double): DrawRobot {
            return this
        }

        override fun toString(): String {
            return "DrawRobot(position=$position, orientation=$orientation)"
        }
    }

    private fun getDrawRobot(robot: Robot): DrawRobot {
        return DrawRobot(
            Point(robot.point).shift(robot.direction, PlottingConstraints.CURVE_FIRST_POINT),
            if (robot.beforePoint) robot.direction.opposite().toAngle() else robot.direction.toAngle(),
            robot
        )
    }

    fun importRobot(planet: Planet?, robot: Robot?) {
        this.planet = planet

        if (robot == null) {
            view.setColor(ViewColor.TRANSPARENT)
        } else {
            val drawable = getDrawRobot(robot)

            if (view.color == ViewColor.TRANSPARENT) {
                view.setRobot(drawable, 0.0)
            } else {
                view.setRobot(drawable)
            }

            view.number = robot.groupNumber
            view.setColor(ViewColor.ROBOT_MAIN_COLOR)
            view.requestRedraw()
        }
    }
}

fun Direction.toAngle() = when (this) {
    Direction.NORTH -> 0.0
    Direction.EAST -> PI * 1.5
    Direction.SOUTH -> PI
    Direction.WEST -> PI * 0.5
}

fun Point.toAngle() = atan2(y, x) - PI / 2
