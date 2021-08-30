package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.live.toAngle
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.log2
import de.robolab.client.renderer.drawable.utils.power2
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.utils.Color
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.Vector
import kotlin.math.*

class LegendView(
    position: Vector,
    columnCount: Int
) : BaseView() {


    private val positionTransition = transition(position)
    val position by positionTransition
    fun setPosition(position: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        positionTransition.animate(position, duration, offset)
    }

    private var columnCount = columnCount
    fun setColumnCount(columnCount: Int) {
        this.columnCount = columnCount
        updatePosition()
        requestRedraw()
    }

    private val itemDimension = Vector(2.0, -0.4)
    private val itemRendererList: List<Pair<String, (context: DrawContext, box: Rectangle) -> Unit>>

    private var backgroundRect = Rectangle.ZERO
    private fun updatePosition(){
        backgroundRect = Rectangle.fromDimension(
            position,
            itemDimension * Vector(columnCount, (itemRendererList.size + columnCount - 1) / columnCount)
        )
    }

    override fun onDraw(context: DrawContext) {
        context.fillRect(backgroundRect, context.theme.plotter.primaryBackgroundColor)

        for ((index, item) in itemRendererList.withIndex()) {
            val col = index % columnCount
            val row = index / columnCount

            val pos = position + Vector(itemDimension.left * col, itemDimension.top * row)
            val box = Rectangle.fromDimension(pos, itemDimension)
            renderItem(context, box, item.first, item.second)
        }
    }

    private fun renderItem(context: DrawContext, box: Rectangle, name: String, render: (context: DrawContext, box: Rectangle) -> Unit) {
        context.strokeRect(box.shrink(0.02), Color.PURPLE, 0.025)

        val renderBox = Rectangle.fromDimension(
            box.bottomLeft + Vector(0.05, -0.05),
            Vector(-itemDimension.height - 0.1, itemDimension.height + 0.1)
        )
        context.strokeRect(renderBox, Color.RED, 0.01)
        render(context, renderBox)

        context.fillText(
            name,
            Vector(box.left + 0.4, (box.top + box.bottom) / 2.0),
            context.theme.plotter.lineColor,
            alignment = ICanvas.FontAlignment.LEFT
        )
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    private fun renderLegendSender(context: DrawContext, box: Rectangle) {
        val origin = box.center - Vector(0.04, 0.04)
        context.fillRect(
            Rectangle.fromCenter(origin, PlottingConstraints.POINT_SIZE * SCALE, PlottingConstraints.POINT_SIZE * SCALE),
            context.theme.plotter.redColor
        )

        SenderView.satellite(
            context,
            origin + (Vector(PlottingConstraints.POINT_SIZE / 2.5, PlottingConstraints.POINT_SIZE / 2.5) * SCALE),
            0.0,
            PI / 2.0,
            ViewColor.EDIT_COLOR,
            'A',
            emptyList(),
            0.0,
            SCALE
        )
    }

    private fun renderLegendTarget(context: DrawContext, box: Rectangle) {
        context.fillArc(
            box.center,
            PlottingConstraints.TARGET_RADIUS * SCALE,
            0.0,
            2.0 * PI,
            context.theme.plotter.editColor
        )
        context.fillRect(
            Rectangle.fromCenter(
                box.center,
                PlottingConstraints.POINT_SIZE * SCALE,
                PlottingConstraints.POINT_SIZE * SCALE
            ),
            context.theme.plotter.redColor
        )
    }

    private fun renderLegendBlocked(context: DrawContext, box: Rectangle) {
        val top = box.center + Vector(0.0, 0.06)
        val bottom = box.center - Vector(0.0, 0.08)

        context.strokeLine(
            listOf(top, bottom),
            context.theme.plotter.lineColor,
            PlottingConstraints.LINE_WIDTH * SCALE
        )

        context.fillRect(
            Rectangle.fromCenter(top, PlottingConstraints.POINT_SIZE * SCALE, PlottingConstraints.POINT_SIZE / 3.0),
            context.theme.plotter.redColor
        )

        BlockedView.drawBlocked(
            context,
            bottom,
            ViewColor.POINT_RED,
            false,
            null,
            SCALE
        )
    }

    private fun renderLegendPathSelect(context: DrawContext, box: Rectangle) {
        val top = box.center + Vector(0.0, 0.06)
        val bottom = box.center - Vector(0.0, 0.08)

        context.strokeLine(
            listOf(top, bottom),
            context.theme.plotter.lineColor,
            PlottingConstraints.LINE_WIDTH * SCALE
        )

        context.fillRect(
            Rectangle.fromCenter(top, PlottingConstraints.POINT_SIZE * SCALE, PlottingConstraints.POINT_SIZE / 3.0),
            context.theme.plotter.redColor
        )

        val (arrowStart, arrowTarget) = listOf(
            Vector(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6),
            Vector(PlottingConstraints.POINT_SIZE * 0.35, PlottingConstraints.POINT_SIZE * 0.6 + PlottingConstraints.ARROW_LENGTH)
        ).map { it.rotate(PlanetDirection.South.toAngle()) * SCALE + top }

        ArrowView.draw(
            context,
            arrowStart,
            arrowTarget,
            PlottingConstraints.LINE_WIDTH * 0.65 * SCALE,
            ViewColor.LINE_COLOR
        )
    }

    private fun renderLegendStart(context: DrawContext, box: Rectangle) {
        val top = box.center + Vector(0.0, 0.06)
        val bottom = box.center - Vector(0.0, 0.08)

        context.strokeLine(
            listOf(top, bottom),
            context.theme.plotter.lineColor,
            PlottingConstraints.LINE_WIDTH * SCALE
        )

        context.fillRect(
            Rectangle.fromCenter(top, PlottingConstraints.POINT_SIZE * SCALE, PlottingConstraints.POINT_SIZE / 3.0),
            context.theme.plotter.redColor
        )
    }

    init {
        animationTime = 0.0
        var list: List<Pair<String, (context: DrawContext, box: Rectangle) -> Unit>> = emptyList()

        list = list + Pair("Sender", this::renderLegendSender)
        list = list + Pair("PathUnveiled", this::renderLegendSender)
        list = list + Pair("Meteoritenschauer", this::renderLegendSender)
        list = list + Pair("Pfadgewicht", this::renderLegendSender)
        list = list + Pair("Target", this::renderLegendTarget)
        list = list + Pair("Pfad blockiert", this::renderLegendBlocked)
        list = list + Pair("PathSelect", this::renderLegendPathSelect)
        list = list + Pair("Start", this::renderLegendStart)
        list = list + Pair("Nicht geklebt", this::renderLegendSender)
        list = list + Pair("Koordinatensystem", this::renderLegendSender)


        itemRendererList = list

        positionTransition.onChange {
            updatePosition()
        }
        updatePosition()
    }

    companion object {
        const val SCALE = 0.35
    }
}
