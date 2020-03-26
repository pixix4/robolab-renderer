package de.robolab.renderer.drawable

import de.robolab.model.Coordinate
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.GroupDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.property.property

@Suppress("LeakingThis")
open class PlanetDrawable() : GroupDrawable() {

    val hoveredPathsProperty = property(emptySet<Path>())
    var hoveredPaths by hoveredPathsProperty

    val selectedPathProperty = property<Path?>(null)
    var selectedPath by selectedPathProperty

    val selectedPointProperty = property<Coordinate?>(null)
    var selectedPoint by selectedPointProperty

    var plotter: DefaultPlotter? = null

    val animationTime: Double
        get() = plotter?.animationTime ?: 0.0

    protected val planetBackground = BackgroundDrawable(this)

    private val pointDrawable = PointDrawable(this)
    private val pathDrawable = PathListDrawable(this)
    private val targetDrawable = TargetDrawable(this)
    private val senderDrawable = SenderDrawable(this)
    private val pathSelectDrawable = PathSelectDrawable(this)

    protected val planetForeground = listOf<IDrawable>(
            targetDrawable,
            senderDrawable,
            pathDrawable,
            pathSelectDrawable,
            pointDrawable
    )

    protected val viewBackground = listOf<IDrawable>(
            GridLinesDrawable
    )

    protected val viewForeground = listOf<IDrawable>(
            GridNumbersDrawable,
            CompassDrawable(this)
    )

    override val drawableList = listOf(
            planetBackground,
            *viewBackground.toTypedArray(),
            *planetForeground.toTypedArray(),
            *viewForeground.toTypedArray()
    )

    private lateinit var pointerListener: EventListener<*>

    override fun onAttach(plotter: DefaultPlotter) {
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
    }

    override fun onDetach(plotter: DefaultPlotter) {
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
        planetBackground.importPlanet(planet)
        pointDrawable.importPlanet(planet)
        pathDrawable.importPlanet(planet)
        targetDrawable.importPlanet(planet)
        senderDrawable.importPlanet(planet)
        pathSelectDrawable.importPlanet(planet)

        center = BackgroundDrawable.calcPlanetArea(planet)?.center ?: Point.ZERO

        val currentSelectedPath = selectedPath ?: return
        selectedPath = null
        for (path in planet.pathList) {
            if (path.equalPath(currentSelectedPath)) {
                selectedPath = path
            }
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
