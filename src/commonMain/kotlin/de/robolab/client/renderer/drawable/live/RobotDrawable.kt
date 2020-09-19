package de.robolab.client.renderer.drawable.live

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.utils.BSpline
import de.robolab.client.renderer.drawable.utils.shift
import de.robolab.client.renderer.transition.IInterpolatable
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.RobotView
import de.robolab.common.planet.*
import de.robolab.common.utils.Point
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class RobotDrawable(
    reference: Robot? = null
) : Animatable<RobotDrawable.Robot?>(reference) {

    override val view = RobotView(
        DrawRobot(Point.ZERO, 0.0, null),
        null,
        ViewColor.TRANSPARENT
    )

    data class Robot(
        val point: Coordinate,
        val direction: Direction,
        val beforePoint: Boolean,
        val groupNumber: Int?,
        val backwardMotion: Boolean,
    ) {
        val afterPoint: Boolean = !beforePoint
    }

    private var planet: Planet? = null

    inner class DrawRobot(
        val position: Point,
        val orientation: Double,
        private val robot: Robot?
    ) : IInterpolatable<DrawRobot> {

        fun getAnimationTimeMultiplier(toValue: DrawRobot): Double {
            if (position == toValue.position && orientation == toValue.orientation) {
                return 1.0
            }

            if (toValue.robot?.backwardMotion == true ) {
                return toValue.getAnimationTimeMultiplier(DrawRobot(
                    position,
                    orientation,
                    robot?.copy(backwardMotion = false)
                ))
            }

            if (robot != null && toValue.robot != null) {
                if (robot.beforePoint || toValue.robot.afterPoint || toValue.robot.backwardMotion) {
                    return 1.0
                }

                var path = Path(
                    robot.point,
                    robot.direction,
                    toValue.robot.point,
                    toValue.robot.direction,
                    1,
                    emptySet(),
                    emptyList(),
                    hidden = false,
                    showDirectionArrow = false
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

                val points = PathAnimatable.getControlPointsFromPath(planet?.version ?: PlanetVersion.CURRENT, path)
                val length = path.length(points)

                return max(0.5, min(10.0, length))
            }

            return 1.0
        }

        override fun interpolate(toValue: DrawRobot, progress: Double): DrawRobot {
            if (position == toValue.position && orientation == toValue.orientation) {
                return this
            }

            if (toValue.robot?.backwardMotion == true ) {
                return toValue.interpolate(DrawRobot(
                    position,
                    orientation,
                    robot?.copy(backwardMotion = false)
                ), 1.0 - progress)
            }

            val orientationDelta = when {
                orientation - toValue.orientation > PI -> toValue.orientation - (orientation - 2 * PI)
                toValue.orientation - orientation > PI -> (toValue.orientation - 2 * PI) - orientation
                else -> toValue.orientation - orientation
            }

            if (robot != null && toValue.robot != null) {
                if (robot.beforePoint || toValue.robot.afterPoint || toValue.robot.backwardMotion) {
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
                    hidden = false,
                    showDirectionArrow = false
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

                val points = PathAnimatable.getControlPointsFromPath(planet?.version ?: PlanetVersion.CURRENT, path)

                val position: Point
                val d: Point

                if (path.isOneWayPath) {
                    val driveInterval = 0.4
                    when {
                        progress < driveInterval -> {
                            val t = progress / driveInterval
                            position = BSpline.eval(t, points)
                            d = BSpline.evalGradient(t, points)
                        }
                        progress < 1.0 - driveInterval -> {
                            val t = (progress - driveInterval) / (1.0 - driveInterval - driveInterval)
                            position = BSpline.eval(1.0, points)
                            val o = BSpline.evalGradient(1.0, points)
                            d = o.rotate(PI * t)
                        }
                        else -> {
                            val t = (1.0 - progress) / driveInterval
                            position = BSpline.eval(t, points)
                            d = BSpline.evalGradient(t, points).inverse()
                        }
                    }
                } else {
                    position = BSpline.eval(progress, points)
                    d = BSpline.evalGradient(progress, points)
                }

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
            robot,
        )
    }

    override fun onUpdate(obj: Robot?, planet: Planet) {
        super.onUpdate(obj, planet)

        importRobot(planet, obj)
    }

    private var lastRobot: Robot? = null
    fun importRobot(planet: Planet?, robot: Robot?) {
        this.planet = planet

        if (robot == lastRobot) {
            return
        }
        lastRobot = robot

        if (robot == null) {
            view.setColor(ViewColor.TRANSPARENT)
        } else {
            val drawable = getDrawRobot(robot)

            val animationTimeMultiplier = view.robot.getAnimationTimeMultiplier(drawable)
            if (view.color == ViewColor.TRANSPARENT) {
                view.setRobot(drawable, 0.0)
            } else {
                view.setRobot(drawable, view.animationTime * animationTimeMultiplier)
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

fun Point.toAngle() = (atan2(y, x) + 3 * PI / 2) % (2 * PI)
