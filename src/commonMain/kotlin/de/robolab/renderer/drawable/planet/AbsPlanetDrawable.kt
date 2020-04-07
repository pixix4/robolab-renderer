package de.robolab.renderer.drawable.planet

import de.robolab.model.Path
import de.robolab.planet.Planet
import de.robolab.renderer.IPlotter
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.*
import de.robolab.renderer.drawable.base.GroupDrawable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.base.IDrawable
import de.westermann.kobserve.property.property

@Suppress("LeakingThis")
abstract class AbsPlanetDrawable() : GroupDrawable(), IAnimationTime {

    val drawCompassProperty = property(true)
    var drawCompass by drawCompassProperty

    val drawNameProperty = property(false)
    var drawName by drawNameProperty

    val drawGridLinesProperty = property(true)
    var drawGridLines by drawGridLinesProperty

    val drawGridNumbersProperty = property(true)
    var drawGridNumbers by drawGridNumbersProperty

    var plotter: IPlotter? = null

    val selectedElementsProperty = property(emptyList<Any>())
    override var selectedElements by selectedElementsProperty

    override val animationTime: Double
        get() = plotter?.animationTime ?: 0.0

    private val backgroundDrawable = BackgroundDrawable(this)
    private val gridLinesDrawable = GridLinesDrawable
    private val nameDrawable = NameDrawable()
    private val gridNumbersDrawable = GridNumbersDrawable
    private val compassDrawable = CompassDrawable(this)

    private var planetLayers: List<PlanetLayer> = emptyList()
    private var overlays: List<IDrawable> = emptyList()
    protected fun buildDrawableList(
            planetLayers: List<PlanetLayer> = this.planetLayers,
            overlays: List<IDrawable> = this.overlays
    ){
        this.planetLayers = planetLayers
        this.overlays = overlays

        val list = mutableListOf<IDrawable>(backgroundDrawable)

        if (drawGridLinesProperty.value) {
            list += gridLinesDrawable
        }

        list += planetLayers
        list += overlays

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

    override fun onAttach(plotter: IPlotter) {
        this.plotter = plotter

        centerPlanet()
    }

    override fun onDetach(plotter: IPlotter) {
        this.plotter = null
    }

    override fun onUpdate(ms_offset: Double): Boolean {
        val hsc = redraw
        redraw = false
        return super.onUpdate(ms_offset) || hsc
    }

    private var redraw = false

    private var center = Point.ZERO

    fun centerPlanet(duration: Double = 0.0) {
        val transformation = plotter?.transformation ?: return
        val canvasCenter = center * transformation.scaledGridWidth * Point(-1.0, 1.0)
        val size = (plotter?.size ?: Dimension.ZERO) / 2

        transformation.translateTo(canvasCenter + size, duration)
    }

    protected fun importPlanets() {
        val planetList = planetLayers.map { it.planet }

        backgroundDrawable.importPlanet(planetList)
        nameDrawable.importPlanet(planetList.asReversed().firstOrNull { it.name.isNotEmpty() } ?: Planet.EMPTY)

        center = BackgroundDrawable.calcPlanetArea(planetList)?.center ?: Point.ZERO

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

        plotter?.updatePointer()
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
        centerPlanet()
    }
}
