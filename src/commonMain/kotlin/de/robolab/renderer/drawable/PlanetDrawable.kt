package de.robolab.renderer.drawable

import de.robolab.model.Coordinate
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.IPlotter
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.GroupDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.base.IPlanetDrawable
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.property.property

@Suppress("LeakingThis")
open class PlanetDrawable(
        var drawCompass: Boolean = true,
        var drawName: Boolean = false,
        var drawGridLines: Boolean = true,
        var drawGridNumbers: Boolean = true
) : IPlanetDrawable() {

    val hoveredPathsProperty = property(emptySet<Path>())
    var hoveredPaths by hoveredPathsProperty

    val selectedPathProperty = property<Path?>(null)
    var selectedPath by selectedPathProperty

    val selectedPointProperty = property<Coordinate?>(null)
    var selectedPoint by selectedPointProperty

    var plotter: IPlotter? = null

    override val animationTime: Double
        get() = plotter?.animationTime ?: 0.0

    private val backgroundDrawable = BackgroundDrawable(this)
    private val pointDrawable = PointAnimatableManager(this)
    private val pathDrawable = PathAnimatableManager(this)
    private val targetDrawable = TargetAnimatableManager(this)
    private val senderDrawable = SenderAnimatableManager(this)
    private val pathSelectDrawable = PathSelectAnimatableManager(this)
    private val nameDrawable = NameDrawable(this)

    val planetBackground: IDrawable = backgroundDrawable

    val planetForeground: IDrawable = GroupDrawable(
            targetDrawable,
            senderDrawable,
            pathDrawable,
            pathSelectDrawable,
            pointDrawable
    )

    val viewBackground: IDrawable = GroupDrawable(
            GridLinesDrawable(this)
    )

    val viewForeground: IDrawable = GroupDrawable(
            GridNumbersDrawable(this),
            CompassDrawable(this),
            nameDrawable
    )

    override val drawableList = listOf(
            planetBackground,
            viewBackground,
            planetForeground,
            viewForeground
    )

    private lateinit var pointerListener: EventListener<*>

    override fun onAttach(plotter: IPlotter) {
        this.plotter = plotter
        pointerListener = plotter.pointerProperty.onChange.reference {
            val path = plotter.pointer.findObjectUnderPointer<Path>(true)
            val newElements = if (path == null) {
                emptySet()
            } else {
                setOf(path)
            }

            hoveredPaths = newElements
        }

        centerPlanet()
    }

    override fun onDetach(plotter: IPlotter) {
        this.plotter = null
        pointerListener.detach()
    }

    override fun onUpdate(ms_offset: Double): Boolean {
        val hsc = hasSelectedChanged
        hasSelectedChanged = false
        return super.onUpdate(ms_offset) || hsc
    }

    private var hasSelectedChanged = false

    private var center = Point.ZERO

    fun centerPlanet(duration: Double = 0.0) {
        val transformation = plotter?.transformation ?: return
        val canvasCenter = center * transformation.scaledGridWidth * Point(-1.0, 1.0)
        val size = (plotter?.size ?: Dimension.ZERO) / 2

        transformation.translateTo(canvasCenter + size, duration)
    }

    open fun importPlanet(planet: Planet) {
        backgroundDrawable.importPlanet(planet)
        pointDrawable.importPlanet(planet)
        pathDrawable.importPlanet(planet)
        targetDrawable.importPlanet(planet)
        senderDrawable.importPlanet(planet)
        pathSelectDrawable.importPlanet(planet)
        nameDrawable.importPlanet(planet)

        center = BackgroundDrawable.calcPlanetArea(planet)?.center ?: Point.ZERO

        val currentSelectedPath = selectedPath ?: return
        selectedPath = null
        for (path in planet.pathList) {
            if (path.equalPath(currentSelectedPath)) {
                selectedPath = path
            }
        }
        if (planet.startPoint?.path?.equalPath(currentSelectedPath) == true) {
            selectedPath = planet.startPoint.path
        }

        plotter?.updatePointer()
    }

    init {
        selectedPathProperty.onChange {
            hasSelectedChanged = true
        }
        selectedPointProperty.onChange {
            hasSelectedChanged = true
        }
        hoveredPathsProperty.onChange {
            hasSelectedChanged = true
        }
    }

    override fun onResize(size: Dimension) {
        centerPlanet()
    }
}
