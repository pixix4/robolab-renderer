package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.utils.BSpline
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.client.renderer.utils.ITransformationReference
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.renderer.view.base.Document
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.extraGet
import de.robolab.client.renderer.view.component.*
import de.robolab.common.planet.IPlanetValue
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.Vector
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@Suppress("LeakingThis")
abstract class AbsPlanetDrawable(
    transformationStateProperty: ObservableProperty<Transformation.State> = property(Transformation.State.DEFAULT)
) : ITransformationReference {

    private var transformationState by transformationStateProperty

    val drawCompassProperty = property(true)
    var drawCompass by drawCompassProperty

    val drawNameProperty = property(false)
    var drawName by drawNameProperty

    val drawGridLinesProperty = property(true)
    var drawGridLines by drawGridLinesProperty

    val drawGridNumbersProperty = property(true)
    var drawGridNumbers by drawGridNumbersProperty

    val drawBackgroundProperty = property(true)
    var drawBackground by drawBackgroundProperty

    val plotterProperty = property<PlotterWindow?>(null)
    var plotter by plotterProperty

    val transformationProperty = plotterProperty.mapBinding { it?.transformation }
    override val transformation by transformationProperty

    val pointerProperty = plotterProperty.nullableFlatMapBinding { it?.pointerProperty }
    val pointer by pointerProperty

    val flipViewProperty = transformationProperty.nullableFlatMapBinding { it?.flipViewProperty }
    val flipView by flipViewProperty.mapBinding { it == true }
    fun flip(flipped: Boolean? = null) {
        transformation?.flipViewProperty?.value = flipped ?: !flipView
        centerPlanet()
    }


    private val backgroundView = RectangleView(null, ViewColor.PRIMARY_BACKGROUND_COLOR)
    private val gridLinesView = GridLineView()
    private val nameView = TextView(
        Point.ZERO,
        40.0,
        "",
        ViewColor.LINE_COLOR.interpolate(ViewColor.GRID_TEXT_COLOR, 0.5),
        ICanvas.FontAlignment.RIGHT,
        ICanvas.FontWeight.BOLD
    )
    private val gridNumbersView = GridNumberView()
    private val compassView = CompassView()

    private var planetLayers: List<IPlanetLayer> = emptyList()

    fun setPlanetLayers(vararg planetLayer: IPlanetLayer) {
        planetLayers = planetLayer.toList()

        planetLayerViews.clear()
        for (layer in planetLayer) {
            planetLayerViews += layer.view
        }
    }

    protected val backgroundViews = GroupView("Background views")
    protected val underlayerViews = GroupView("Underlayer views")
    private val planetLayerViews = GroupView("Planet layer views")
    protected val overlayerViews = GroupView("Overlayer views")

    val view = Document(
        this,
        ConditionalView("Planet background", drawBackgroundProperty, backgroundView),
        backgroundViews,
        ConditionalView("Grid lines", drawGridLinesProperty, gridLinesView),
        underlayerViews,
        planetLayerViews,
        overlayerViews,
        ConditionalView("Grid numbers", drawGridNumbersProperty, gridNumbersView),
        ConditionalView("Compass", drawCompassProperty, compassView),
        ConditionalView("Planet name", drawNameProperty, nameView),
    )

    var focusedElementsProperty = view.focusedStack.mapBinding { list ->
        list.mapNotNull { it.extraGet<IPlanetValue>() }
    }

    fun focus(value: IPlanetValue) {
        for (layer in planetLayers) {
            layer.focus(value)
        }
    }


    private var centerOfPlanets: Vector? = null

    override var autoCentering = true
    override fun centerPlanet(duration: Double) {
        val centerOfPlanets = centerOfPlanets ?: return
        val transformation = plotter?.transformation ?: return
        val targetCenter = centerOfPlanets.rotate(transformation.rotation)
        val size = (plotter?.dimension ?: Dimension.ZERO) / Point(2.0, -2.0) * Point(if (flipView) -1.0 else 1.0, 1.0)
        val point = (targetCenter * transformation.scaledGridWidth - size) * Point(if (flipView) 1.0 else -1.0, 1.0)

        transformation.translateTo(point, duration)
    }

    private var isFirstImport = true
    protected fun importPlanets() {
        val planetList = planetLayers.map { it.planet }

        val area = calcPlanetArea(planetList)
        val paperArea = area?.expand(1.0)
        backgroundView.setRectangle(paperArea)
        nameView.text = planetList.asReversed().firstOrNull { it.name.isNotEmpty() }?.name ?: ""
        val edge = paperArea?.bottomRight ?: Point.ZERO
        nameView.setSource(edge - Point(0.3, 0.4))

        centerOfPlanets = area?.center
        if (autoCentering) {
            centerPlanet(if (isFirstImport) 0.0 else TransformationInteraction.ANIMATION_TIME)
        }

        plotter?.updatePointer()
        isFirstImport = false

        focusedElementsProperty.invalidate()
    }

    init {
        view.onAttach { plotter ->
            this.plotter = plotter

            plotter.transformation.import(transformationState)
            if (transformationState.isDefault()) {
                centerPlanet()
            }
        }

        view.onDetach { plotter ->
            this.plotter = null

            transformationState = plotter.transformation.export()
        }

        view.onCanvasResize {
            if (autoCentering) {
                centerPlanet()
            }
        }

        view.onUserTransformation {
            autoCentering = false
        }

        compassView.onPointerDown { event ->
            event.stopPropagation()
        }

        compassView.onPointerUp { event ->
            event.stopPropagation()

            val transformation = transformation ?: return@onPointerUp
            val currentAngle = round(transformation.rotation / PI * 180.0 * 100.0) / 100.0
            val newAngle = ((currentAngle - 180.0) % 360.0 + 180.0) % 360.0
            transformation.rotateTo(newAngle / 180.0 * PI, event.screen / 2)
            if (newAngle != 0.0) {
                transformation.rotateTo(0.0, event.screen / 2, 250.0)
            } else {
                autoCentering = true
                centerPlanet(TransformationInteraction.ANIMATION_TIME)
            }
        }
    }

    open fun calcPlanetArea(planetList: List<Planet>): Rectangle? {
        val areaList = planetList.mapNotNull { calcPlanetArea(it) }
        return if (areaList.isEmpty()) {
            null
        } else {
            areaList.reduce { acc, rectangle ->
                acc.union(rectangle)
            }
        }
    }

    companion object {

        fun calcPlanetArea(planet: Planet): Rectangle? {
            var minX = Double.MAX_VALUE
            var minY = Double.MAX_VALUE
            var maxX = -Double.MAX_VALUE
            var maxY = -Double.MAX_VALUE
            var found = false

            fun update(x: Double, y: Double) {
                minX = min(minX, x)
                minY = min(minY, y)
                maxX = max(maxX, x)
                maxY = max(maxY, y)
                found = true
            }

            for (p in planet.pathList) {
                if (p.hidden) continue

                update(p.source.x.toDouble(), p.source.y.toDouble())
                update(p.target.x.toDouble(), p.target.y.toDouble())

                for (e in p.exposure) {
                    update(e.x.toDouble(), e.y.toDouble())
                }

                val controlPoints = PathAnimatable.getControlPointsFromPath(planet.version, p)
                val points = PathAnimatable.multiEval(16, controlPoints, Point(p.source), Point(p.target)) {
                    BSpline.eval(it, controlPoints)
                }
                for (c in points) {
                    update(c.left, c.top)
                }
            }

            if (planet.startPoint != null) {
                update(planet.startPoint.point.x.toDouble(), planet.startPoint.point.y.toDouble())

                if (planet.startPoint.controlPoints.isNotEmpty()) {
                    val p = planet.startPoint.path
                    val controlPoints = PathAnimatable.getControlPointsFromPath(planet.version, p)
                    val points = PathAnimatable.multiEval(16, controlPoints, Point(p.source), Point(p.target)) {
                        BSpline.eval(it, controlPoints)
                    }
                    for (c in points) {
                        update(c.left, c.top)
                    }
                }
            }

            if (!found) {
                return null
            }

            minX = round(minX * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            minY = round(minY * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            maxX = round(maxX * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            maxY = round(maxY * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR

            return Rectangle(minX, minY, maxX - minX, maxY - minY)
        }
    }
}
