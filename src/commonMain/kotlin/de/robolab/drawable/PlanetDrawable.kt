package de.robolab.drawable

import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.GroupDrawable
import de.robolab.renderer.drawable.IRootDrawable
import de.westermann.kobserve.event.EventListener

@Suppress("LeakingThis")
open class PlanetDrawable() : IRootDrawable {

    interface ISelectionCallback {
        fun onPathHoverEnter(path: Path)
        fun onPathHoverExit(path: Path)
    }

    var selectionCallback: ISelectionCallback = object : ISelectionCallback {
        override fun onPathHoverEnter(path: Path) {

        }

        override fun onPathHoverExit(path: Path) {

        }
    }

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
    var hoveredElements: Set<Any> = emptySet()
    override fun onAttach(plotter: DefaultPlotter) {
        this.plotter = plotter
        pointerListener = plotter.pointerProperty.onChange.reference {
            val newElements = if (plotter.pointer.objectUnderPointer == null) {
                emptySet()
            } else {
                listOfNotNull(plotter.pointer.objectUnderPointer).toSet()
            }

            if (newElements != hoveredElements) {
                hoveredElements = newElements
                importPlanet()
            }
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

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
        return drawable.getObjectAtPosition(context, position)
    }

    private var lastPlanet: Planet? = null
    private fun importPlanet() {
        lastPlanet?.let {
            importPlanet(it)
        }
    }


    open fun importPlanet(planet: Planet) {
        lastPlanet = planet
        planetBackground.importPlanet(planet)
        pointDrawable.importPlanet(planet)
        pathDrawable.importPlanet(planet)
        targetDrawable.importPlanet(planet)
        senderDrawable.importPlanet(planet)
        pathSelectDrawable.importPlanet(planet)
    }
}
