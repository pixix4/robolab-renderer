package de.robolab.drawable

import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.DrawContext
import de.robolab.renderer.Pointer
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.drawable.IRootDrawable
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.property.property

@Suppress("LeakingThis")
open class PlanetDrawable() : IRootDrawable {

    val hoveredPathsProperty = property(emptySet<Path>())
    var hoveredPaths by hoveredPathsProperty

    val selectedPathProperty = property<Path?>(null)
    var selectedPath by selectedPathProperty

    var plotter: DefaultPlotter? = null

    val animationTime: Double
        get() = plotter?.animationTime ?: 0.0

    protected val planetBackground = BackgroundDrawable(this)

    private val pointDrawable = PointDrawable(this)
    private val pathDrawable = PathListDrawable(this)
    private val targetDrawable = TargetDrawable(this)
    private val senderDrawable = SenderDrawable(this)
    private val pathSelectDrawable = PathSelectDrawable(this)

    protected val planetForeground = GroupDrawable(
            targetDrawable,
            senderDrawable,
            pathDrawable,
            pathSelectDrawable,
            pointDrawable
    )

    protected val viewBackground = GroupDrawable(
            GridLinesDrawable
    )

    protected val viewForeground = GroupDrawable(
            GridNumbersDrawable,
            CompassDrawable
    )

    open val drawable = GroupDrawable(
            planetBackground,
            viewBackground,
            planetForeground,
            viewForeground
    )

    private lateinit var pointerListener: EventListener<*>

    override fun onAttach(plotter: DefaultPlotter) {
        this.plotter = plotter
        pointerListener = plotter.pointerProperty.onChange.reference {
            val path = plotter.pointer.findObjectUnderPointer<Path>()
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
        return drawable.onUpdate(ms_offset)
    }

    override fun onDraw(context: DrawContext) {
        drawable.onDraw(context)
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return drawable.getObjectsAtPosition(context, position)
    }

    open fun importPlanet(planet: Planet) {
        planetBackground.importPlanet(planet)
        pointDrawable.importPlanet(planet)
        pathDrawable.importPlanet(planet)
        targetDrawable.importPlanet(planet)
        senderDrawable.importPlanet(planet)
        pathSelectDrawable.importPlanet(planet)

        val currentSelectedPath = selectedPath ?: return
        for (path in planet.pathList) {
            if (path.equalPath(currentSelectedPath)) {
                selectedPath = path
            }
        }

        plotter?.updatePointer()
    }
}
