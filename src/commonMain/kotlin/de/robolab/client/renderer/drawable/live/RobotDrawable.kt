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
import de.robolab.common.planet.utils.PlanetVersion
import de.robolab.common.utils.Vector
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class RobotDrawable(
    reference: Robot? = null
) : Animatable<RobotDrawable.Robot?>(reference) {

    override val view = RobotView(
        DrawRobot(Vector.ZERO, 0.0, null),
        null,
        ViewColor.TRANSPARENT
    )

    data class Robot(
        val point: PlanetPoint,
        val direction: PlanetDirection,
        val beforePoint: Boolean,
        val groupNumber: Int?,
        val backwardMotion: Boolean,
    ) {
        val afterPoint: Boolean = !beforePoint
    }

    private var planet: Planet? = null

    inner class DrawRobot(
        val position: Vector,
        val orientation: Double,
        private val robot: Robot?
    ) : IInterpolatable<DrawRobot> {

        private var cachedToRobot: Robot? = null
        private var cachedToRobotPathPoints: Pair<PlanetPath, List<Vector>>? = null
        private fun getPathPoints(toRobot: Robot): Pair<PlanetPath, List<Vector>> {
            if (robot == null) throw IllegalStateException()

            if (cachedToRobot == toRobot) {
                val cache = cachedToRobotPathPoints
                if (cache != null) {
                    return cache
                }
            } else {
                cachedToRobot = toRobot
                cachedToRobotPathPoints = null
            }

            var path = PlanetPath(
                source = robot.point,
                sourceDirection = robot.direction,
                target = toRobot.point,
                targetDirection = toRobot.direction,
                weight = 1,
                exposure = emptySet(),
                hidden = false,
                spline = null,
                arrow = false
            )
            val planetPath = planet?.paths?.find { it.equalPath(path) }
            if (planetPath != null) {
                path = if (path.source == planetPath.source && path.sourceDirection == planetPath.sourceDirection) {
                    path.copy(
                        spline = planetPath.spline
                    )
                } else {
                    path.copy(
                        spline = planetPath.spline?.reversed()
                    )
                }
            }

            val result = path to PathAnimatable.getControlPointsFromPath(planet?.version ?: PlanetVersion.CURRENT, path)
            cachedToRobotPathPoints = result
            return result
        }

        fun getAnimationTimeMultiplier(toValue: DrawRobot): Double {
            if (position == toValue.position && orientation == toValue.orientation) {
                return 1.0
            }

            if (toValue.robot?.backwardMotion == true) {
                return toValue.getAnimationTimeMultiplier(
                    DrawRobot(
                        position,
                        orientation,
                        robot?.copy(backwardMotion = false)
                    )
                )
            }

            if (robot != null && toValue.robot != null) {
                if (robot.beforePoint || toValue.robot.afterPoint || toValue.robot.backwardMotion) {
                    return 1.0
                }

                val (path, points) = getPathPoints(toValue.robot)
                val length = path.length(points)

                return max(0.5, min(8.0, length))
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

                val (path, points) = getPathPoints(toValue.robot)

                val position: Vector
                val d: Vector

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
            robot.point.point.shift(robot.direction, PlottingConstraints.CURVE_FIRST_POINT),
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

fun PlanetDirection.toAngle() = when (this) {
    PlanetDirection.North -> 0.0
    PlanetDirection.East -> PI * 1.5
    PlanetDirection.South -> PI
    PlanetDirection.West -> PI * 0.5
}

fun Vector.toAngle() = (atan2(y, x) + 3 * PI / 2) % (2 * PI)
