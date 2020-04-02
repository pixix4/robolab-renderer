package de.robolab.renderer.drawable.live

import de.robolab.model.Coordinate
import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.IInterpolatable
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.PathAnimatable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.base.IPlanetDrawable
import de.robolab.renderer.drawable.utils.BSpline
import de.robolab.renderer.drawable.utils.SegmentDrawer
import de.robolab.renderer.drawable.utils.shift
import de.robolab.renderer.utils.DrawContext
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2

class RobotDrawable(
        private val planetDrawable: IPlanetDrawable
) : IDrawable {

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
            val groupNumber: Int?,
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
                            groupNumber,
                            null
                    )
                }

                var path = Path(robot.point, robot.direction, toValue.robot.point, toValue.robot.direction, 1, emptySet(), emptyList(), false)
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
                val d = BSpline.evalD(progress, points)

                return DrawRobot(
                        position,
                        d.toAngle(),
                        groupNumber,
                        null
                )
            }

            return DrawRobot(
                    position.interpolate(toValue.position, progress),
                    orientation + orientationDelta * progress,
                    groupNumber,
                    null
            )
        }

        override fun toString(): String {
            return "DrawRobot(position=$position, orientation=$orientation)"
        }
    }

    private fun getDrawRobot(robot: Robot): DrawRobot {
        return DrawRobot(
                Point(robot.point).shift(robot.direction, PlottingConstraints.CURVE_FIRST_POINT),
                if (robot.beforePoint) robot.direction.opposite().toAngle() else robot.direction.toAngle(),
                robot.groupNumber,
                robot
        )
    }

    private val robotTransition = ValueTransition(DrawRobot(Point.ZERO, 0.0, null, null))
    private val alphaTransition = DoubleTransition(0.0)

    override fun onUpdate(ms_offset: Double): Boolean {
        var changes = false

        changes = robotTransition.update(ms_offset) || changes
        changes = alphaTransition.update(ms_offset) || changes

        return changes
    }

    private fun transform(point: Point, translation: Point, rotation: Double): Point {
        return point.rotate(rotation) * 0.2 + translation
    }

    override fun onDraw(context: DrawContext) {
        if (alphaTransition.value == 0.0) return

        val r = robotTransition.value
        val a = alphaTransition.value

        val trans = { point: Point -> transform(point, r.position, r.orientation) }

        context.fillPolygon(ROBOT_WHEEL.map(trans), context.theme.robotWheelColor.a(a))
        context.fillPolygon(ROBOT_BLOCK.map(trans), context.theme.robotMainColor.a(a))
        context.fillPolygon(ROBOT_DISPLAY.map(trans), context.theme.robotDisplayColor.a(a))

        context.fillPolygon(ROBOT_SENSOR.map(trans), context.theme.robotSensorColor.a(a))

        val c = context.theme.robotButtonColor.a(a)
        context.fillPolygon(ROBOT_CROSS_TOP.map(trans), c)
        context.fillPolygon(ROBOT_CROSS_BOTTOM.map(trans), c)
        context.fillPolygon(ROBOT_CROSS_LEFT.map(trans), c)
        context.fillPolygon(ROBOT_CROSS_RIGHT.map(trans), c)

        if (r.groupNumber == null) {
            context.fillPolygon(ROBOT_EYE_LEFT.map(trans), c)
            context.fillPolygon(ROBOT_EYE_RIGHT.map(trans), c)
        } else {
            val str = r.groupNumber.absoluteValue.toString().padStart(3, '0')

            SegmentDrawer.drawCharacter(
                    context,
                    str[0],
                    c
            ) { point ->
                trans(point * ROBOT_DISPLAY_CHAR_SIZE + ROBOT_DISPLAY_CHAR_1)
            }
            SegmentDrawer.drawCharacter(
                    context,
                    str[1],
                    c
            ) { point ->
                trans(point * ROBOT_DISPLAY_CHAR_SIZE + ROBOT_DISPLAY_CHAR_2)
            }
            SegmentDrawer.drawCharacter(
                    context,
                    str[2],
                    c
            ) { point ->
                trans(point * ROBOT_DISPLAY_CHAR_SIZE + ROBOT_DISPLAY_CHAR_3)
            }
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    fun importRobot(planet: Planet?, robot: Robot?) {
        this.planet = planet

        if (robot == null) {
            alphaTransition.animate(0.0, planetDrawable.animationTime)
        } else {
            val drawable = getDrawRobot(robot)

            if (alphaTransition.value == 0.0) {
                robotTransition.resetValue(drawable)
            } else {
                robotTransition.animate(drawable, planetDrawable.animationTime)
            }

            alphaTransition.animate(1.0, planetDrawable.animationTime)
        }
    }

    companion object {
        val ROBOT_WHEEL = listOf(
                Point(-0.7, 0.0),
                Point(0.7, 0.0),
                Point(0.7, -0.8),
                Point(-0.7, -0.8)
        )
        val ROBOT_BLOCK = listOf(
                Point(-0.5, 0.6),
                Point(0.5, 0.6),
                Point(0.5, -0.9),
                Point(-0.5, -0.9)
        )
        val ROBOT_DISPLAY = listOf(
                Point(-0.35, 0.45),
                Point(0.35, 0.45),
                Point(0.35, 0.0),
                Point(-0.35, 0.0)
        )
        const val ROBOT_DISPLAY_CHAR_SIZE = 0.25
        val ROBOT_DISPLAY_CHAR_2 = Rectangle.fromEdges(ROBOT_DISPLAY).center - Point(ROBOT_DISPLAY_CHAR_SIZE / 2, ROBOT_DISPLAY_CHAR_SIZE / 2)
        val ROBOT_DISPLAY_CHAR_1 = ROBOT_DISPLAY_CHAR_2 - Point(ROBOT_DISPLAY_CHAR_SIZE * 0.8, 0.0)
        val ROBOT_DISPLAY_CHAR_3 = ROBOT_DISPLAY_CHAR_2 + Point(ROBOT_DISPLAY_CHAR_SIZE * 0.8, 0.0)

        val ROBOT_SENSOR = listOf(
                Point(-0.3, 0.6),
                Point(-0.2, 0.7),
                Point(-0.2, 0.8),
                Point(-0.1, 0.9),
                Point(0.1, 0.9),
                Point(0.2, 0.8),
                Point(0.2, 0.7),
                Point(0.3, 0.6),
                Point(0.1, 0.6),
                Point(0.1, 0.7),
                Point(-0.1, 0.7),
                Point(-0.1, 0.6)
        )

        val ROBOT_CROSS_TOP = listOf(
                Point(-0.1, -0.1),
                Point(0.1, -0.1),
                Point(0.1, -0.3),
                Point(0.0, -0.4),
                Point(-0.1, -0.3)
        )
        val ROBOT_CROSS_BOTTOM = listOf(
                Point(-0.1, -0.8),
                Point(0.1, -0.8),
                Point(0.1, -0.6),
                Point(0.0, -0.5),
                Point(-0.1, -0.6)
        )
        val ROBOT_CROSS_LEFT = listOf(
                Point(-0.35, -0.35),
                Point(-0.15, -0.35),
                Point(-0.05, -0.45),
                Point(-0.15, -0.55),
                Point(-0.35, -0.55)
        )
        val ROBOT_CROSS_RIGHT = ROBOT_CROSS_LEFT.map { Point(-it.left, it.top) }

        private const val ROBOT_EYE_THICKNESS = 0.075
        val ROBOT_EYE_LEFT = listOf(
                Point(-0.3, 0.1),
                Point(-0.175, 0.35),
                Point(-0.05, 0.1),
                Point(-0.05 - ROBOT_EYE_THICKNESS, 0.1),
                Point(-0.175, 0.35 - (ROBOT_EYE_THICKNESS * 2)),
                Point(-0.3 + ROBOT_EYE_THICKNESS, 0.1)
        )
        val ROBOT_EYE_RIGHT = ROBOT_EYE_LEFT.map { Point(-it.left, it.top) }

    }
}

fun Direction.toAngle() = when (this) {
    Direction.NORTH -> 0.0
    Direction.EAST -> PI * 1.5
    Direction.SOUTH -> PI
    Direction.WEST -> PI * 0.5
}

fun Point.toAngle() = atan2(y, x) - PI / 2
