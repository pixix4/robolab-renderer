package de.robolab.renderer.drawable.planet

import de.robolab.planet.Path
import de.robolab.planet.Planet
import de.robolab.renderer.IPlotter
import de.robolab.renderer.ITransformationReference
import de.robolab.renderer.TransformationInteraction
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.*
import de.robolab.renderer.drawable.base.GroupDrawable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.utils.Pointer
import de.robolab.renderer.utils.Transformation
import de.westermann.kobserve.property.property

@Suppress("LeakingThis")
abstract class AbsPlanetDrawable() : GroupDrawable(), IAnimationTime, ITransformationReference {

    val drawCompassProperty = property(true)
    var drawCompass by drawCompassProperty

    val drawNameProperty = property(false)
    var drawName by drawNameProperty

    val drawGridLinesProperty = property(true)
    var drawGridLines by drawGridLinesProperty

    val drawGridNumbersProperty = property(true)
    var drawGridNumbers by drawGridNumbersProperty

    var plotter: IPlotter? = null

    override val transformation: Transformation?
        get() = plotter?.transformation

    override val pointer: Pointer?
        get() = plotter?.pointer

    val selectedElementsProperty = property(emptyList<Any>())
    override var selectedElements by selectedElementsProperty

    override val animationTime: Double
        get() = plotter?.animationTime ?: 0.0

    private val backgroundDrawable = BackgroundDrawable(this)
    private val gridLinesDrawable = GridLinesDrawable
    private val nameDrawable = NameDrawable()
    private val gridNumbersDrawable = GridNumbersDrawable
    private val compassDrawable = CompassDrawable(this)

    private var underlayers: List<IDrawable> = emptyList()
    private var planetLayers: List<PlanetLayer> = emptyList()
    private var overlayers: List<IDrawable> = emptyList()
    protected fun buildDrawableList(
            underlayers: List<IDrawable> = this.underlayers,
            planetLayers: List<PlanetLayer> = this.planetLayers,
            overlayers: List<IDrawable> = this.overlayers
    ){
        this.underlayers = underlayers
        this.planetLayers = planetLayers
        this.overlayers = overlayers

        val list = mutableListOf<IDrawable>(backgroundDrawable)

        if (drawGridLinesProperty.value) {
            list += gridLinesDrawable
        }

        list += underlayers
        list += planetLayers
        list += overlayers

        if (drawGridNumbersProperty.value) {
            list += gridNumbersDrawable
        }
        if (drawCompassProperty.value) {
            list += compassDrawable
        }
        if (drawNameProperty.value) {
            list += nameDrawable
        }

        drawableList = list
    }

    override var drawableList = emptyList<IDrawable>()

    private var transformationState = Transformation.State.DEFAULT
    override fun onAttach(plotter: IPlotter) {
        this.plotter = plotter

        plotter.transformation.import(transformationState)
        if (transformationState.isDefault()) {
            centerPlanet()
        }
    }

    override fun onDetach(plotter: IPlotter) {
        this.plotter = null

        transformationState = plotter.transformation.export()
    }

    override fun onUpdate(ms_offset: Double): Boolean {
        val changed = redraw
        redraw = false
        return super.onUpdate(ms_offset) || changed
    }

    private var redraw = false

    private var centerOfPlanets = Point.ZERO

    override var autoCentering = true
    override fun centerPlanet(duration: Double) {
        val transformation = plotter?.transformation ?: return
        val canvasCenter = centerOfPlanets * transformation.scaledGridWidth * Point(-1.0, 1.0)
        val size = (plotter?.size ?: Dimension.ZERO) / 2

        transformation.translateTo(canvasCenter + size, duration)
    }

    private var isFirstImport = true
    protected fun importPlanets() {
        val planetList = planetLayers.map { it.planet }

        backgroundDrawable.importPlanet(planetList)
        nameDrawable.importPlanet(planetList.asReversed().firstOrNull { it.name.isNotEmpty() } ?: Planet.EMPTY)

        centerOfPlanets = BackgroundDrawable.calcPlanetArea(planetList)?.center ?: Point.ZERO

        selectedElements = selectedElements.mapNotNull { current ->
            if (current !is Path) return@mapNotNull current

            for (planet in planetList.asReversed()) {
                for (path in planet.pathList) {
                    if (path.equalPath(current)) {
                        return@mapNotNull path
                    }
                }
                if (planet.startPoint?.path?.equalPath(current) == true) {
                    return@mapNotNull planet.startPoint.path
                }
            }

            return@mapNotNull null
        }

        if (autoCentering) {
            centerPlanet(if (isFirstImport) 0.0 else TransformationInteraction.ANIMATION_TIME)
        }

        plotter?.updatePointer()
        isFirstImport = false
    }
    
    @Suppress("UNUSED_PARAMETER")
    private fun rebuildDrawable(unit: Unit) {
        redraw = true
        buildDrawableList()
    }

    init {
        selectedElementsProperty.onChange{
            redraw = true
        }
        drawCompassProperty.onChange(this::rebuildDrawable)
        drawNameProperty.onChange(this::rebuildDrawable)
        drawGridNumbersProperty.onChange(this::rebuildDrawable)
        drawGridLinesProperty.onChange(this::rebuildDrawable)
    }

    override fun onResize(size: Dimension) {
        if (autoCentering) {
            centerPlanet()
        }
    }

    override fun onUserTransformation() {
        autoCentering = false
    }
}
