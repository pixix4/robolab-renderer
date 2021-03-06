package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.client.renderer.drawable.utils.SegmentDrawer
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable
import kotlin.math.absoluteValue

class RobotView(
    robot: RobotDrawable.DrawRobot,
    var number: Int?,
    color: ViewColor
) : BaseView() {


    val robotTransition = transition(robot)
    val robot by robotTransition
    fun setRobot(robot: RobotDrawable.DrawRobot, duration: Double = animationTime, offset: Double = 0.0) {
        robotTransition.animate(robot, duration, offset)
    }

    val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    private fun transform(point: Vector, translation: Vector, rotation: Double): Vector {
        return point.rotate(rotation) * 0.2 + translation
    }

    override fun onDraw(context: DrawContext) {
        val position = robot.position
        val orientation = robot.orientation

        val alpha = color.toColor(context.theme.plotter).alpha
        val trans = { point: Vector -> transform(point, position, orientation) }

        context.fillPolygon(ROBOT_WHEEL.map(trans), context.theme.plotter.robotWheelColor.a(alpha))
        context.fillPolygon(ROBOT_BLOCK.map(trans), context.theme.plotter.robotMainColor.a(alpha))
        context.fillPolygon(ROBOT_DISPLAY.map(trans), context.theme.plotter.robotDisplayColor.a(alpha))

        context.fillPolygon(ROBOT_SENSOR.map(trans), context.theme.plotter.robotSensorColor.a(alpha))

        val c = context.theme.plotter.robotButtonColor.a(alpha)
        context.fillPolygon(ROBOT_CROSS_TOP.map(trans), c)
        context.fillPolygon(ROBOT_CROSS_BOTTOM.map(trans), c)
        context.fillPolygon(ROBOT_CROSS_LEFT.map(trans), c)
        context.fillPolygon(ROBOT_CROSS_RIGHT.map(trans), c)

        val groupNumber = number
        if (groupNumber == null) {
            context.fillPolygon(ROBOT_EYE_LEFT.map(trans), c)
            context.fillPolygon(ROBOT_EYE_RIGHT.map(trans), c)
        } else {
            val str = groupNumber.absoluteValue.toString().padStart(3, '0')

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

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle.fromEdges(robot.position) unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(robot)
    }

    companion object {
        val ROBOT_WHEEL = listOf(
            Vector(-0.7, 0.0),
            Vector(0.7, 0.0),
            Vector(0.7, -0.8),
            Vector(-0.7, -0.8)
        )
        val ROBOT_BLOCK = listOf(
            Vector(-0.5, 0.6),
            Vector(0.5, 0.6),
            Vector(0.5, -0.9),
            Vector(-0.5, -0.9)
        )
        val ROBOT_DISPLAY = listOf(
            Vector(-0.35, 0.45),
            Vector(0.35, 0.45),
            Vector(0.35, 0.0),
            Vector(-0.35, 0.0)
        )
        const val ROBOT_DISPLAY_CHAR_SIZE = 0.3
        val ROBOT_DISPLAY_CHAR_2 =
            Rectangle.fromEdges(ROBOT_DISPLAY).center - Vector(ROBOT_DISPLAY_CHAR_SIZE / 2, ROBOT_DISPLAY_CHAR_SIZE / 2)
        val ROBOT_DISPLAY_CHAR_1 = ROBOT_DISPLAY_CHAR_2 - Vector(ROBOT_DISPLAY_CHAR_SIZE * 0.8, 0.0)
        val ROBOT_DISPLAY_CHAR_3 = ROBOT_DISPLAY_CHAR_2 + Vector(ROBOT_DISPLAY_CHAR_SIZE * 0.8, 0.0)

        val ROBOT_SENSOR = listOf(
            Vector(-0.3, 0.6),
            Vector(-0.2, 0.7),
            Vector(-0.2, 0.8),
            Vector(-0.1, 0.9),
            Vector(0.1, 0.9),
            Vector(0.2, 0.8),
            Vector(0.2, 0.7),
            Vector(0.3, 0.6),
            Vector(0.1, 0.6),
            Vector(0.1, 0.7),
            Vector(-0.1, 0.7),
            Vector(-0.1, 0.6)
        )

        val ROBOT_CROSS_TOP = listOf(
            Vector(-0.1, -0.1),
            Vector(0.1, -0.1),
            Vector(0.1, -0.3),
            Vector(0.0, -0.4),
            Vector(-0.1, -0.3)
        )
        val ROBOT_CROSS_BOTTOM = listOf(
            Vector(-0.1, -0.8),
            Vector(0.1, -0.8),
            Vector(0.1, -0.6),
            Vector(0.0, -0.5),
            Vector(-0.1, -0.6)
        )
        val ROBOT_CROSS_LEFT = listOf(
            Vector(-0.35, -0.35),
            Vector(-0.15, -0.35),
            Vector(-0.05, -0.45),
            Vector(-0.15, -0.55),
            Vector(-0.35, -0.55)
        )
        val ROBOT_CROSS_RIGHT = ROBOT_CROSS_LEFT.map { Vector(-it.left, it.top) }

        private const val ROBOT_EYE_THICKNESS = 0.075
        val ROBOT_EYE_LEFT = listOf(
            Vector(-0.3, 0.1),
            Vector(-0.175, 0.35),
            Vector(-0.05, 0.1),
            Vector(-0.05 - ROBOT_EYE_THICKNESS, 0.1),
            Vector(-0.175, 0.35 - (ROBOT_EYE_THICKNESS * 2)),
            Vector(-0.3 + ROBOT_EYE_THICKNESS, 0.1)
        )
        val ROBOT_EYE_RIGHT = ROBOT_EYE_LEFT.map { Vector(-it.left, it.top) }

    }
}
