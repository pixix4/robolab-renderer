package de.robolab.app

import de.robolab.file.PlanetFile
import de.robolab.file.demoFile
import de.robolab.file.toFixed
import de.robolab.model.Coordinate
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.ExportPlotter
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.BackgroundDrawable
import de.robolab.renderer.drawable.PlanetDrawable
import de.robolab.renderer.drawable.edit.*
import de.robolab.renderer.drawable.live.LivePlanetDrawable
import de.robolab.renderer.platform.CommonTimer
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.theme.DarkTheme
import de.robolab.renderer.theme.ITheme
import de.robolab.renderer.theme.LightTheme
import de.robolab.renderer.utils.SvgCanvas
import de.robolab.renderer.utils.Transformation
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.roundToInt

class Main(val canvas: ICanvas) {

    private val timer = CommonTimer(50.0)
    private val plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)
    private val editPlanetDrawable = EditPlanetDrawable()
    private val livePlanetDrawable = LivePlanetDrawable()
    private var planetAnimator = PlanetAnimator(Planet.EMPTY)

    private val animationTimer = CommonTimer(1000 / (ANIMATION_TIME * 1.25))

    val animateProperty = property(false)
    val editableProperty = editPlanetDrawable.editableProperty
    val pointerProperty = plotter.pointerProperty.mapBinding { pointer ->
        val list = mutableListOf<String>()
        list.add("Pointer: ${format(pointer.roundedPosition)}")
        if (plotter.transformation.scale != 1.0) {
            list.add("Zoom: ${(plotter.transformation.scale * 100).roundToInt()}%")
        }
        if (plotter.transformation.rotation != 0.0) {
            list.add("Rotation: ${((plotter.transformation.rotation / PI * 180) % 360).roundToInt()}%")
        }
        list.addAll(pointer.objectsUnderPointer.map(this::format))
        list.filter { it.isNotBlank() }
    }

    private fun format(obj: Any): String = when (obj) {
        is Path -> "Path(${obj.source.x},${obj.source.y},${obj.sourceDirection.name.first()} -> ${obj.target.x},${obj.target.y},${obj.targetDirection.name.first()})"
        is Coordinate -> "Coordinate(${obj.x},${obj.y})"
        is Point -> "${obj.left.toFixed(2)},${obj.top.toFixed(2)}"
        is EditDrawEndDrawable.PointEnd -> "PointEnd(${obj.point.x},${obj.point.y} -> ${obj.direction.name.first()})"
        is EditControlPointsDrawable.ControlPoint -> "ControlPoint(index ${obj.point} of ${format(obj.path)})"
        is Menu, is MenuList, is MenuAction -> ""
        else -> obj.toString()
    }

    private val planetFile = PlanetFile(demoFile)

    val themeProperty = PreferenceStorage.selectedThemeProperty
    val lightThemeProperty = property(object : FunctionAccessor<Boolean> {
        override fun get() = themeProperty.value == Theme.LIGHT

        override fun set(value: Boolean): Boolean {
            themeProperty.value = Theme.LIGHT
            return true
        }

    }, themeProperty)
    val darkThemeProperty = property(object : FunctionAccessor<Boolean> {
        override fun get() = themeProperty.value == Theme.DARK

        override fun set(value: Boolean): Boolean {
            themeProperty.value = Theme.DARK
            return true
        }

    }, themeProperty)

    init {
        plotter.drawable = editPlanetDrawable
        plotter.theme = themeProperty.value.theme
        themeProperty.onChange {
            plotter.theme = themeProperty.value.theme
        }

        animateProperty.onChange {
            if (animateProperty.value) {
                editableProperty.value = false

                plotter.drawable = livePlanetDrawable
                planetAnimator = PlanetAnimator(planetFile.planet.value)
                livePlanetDrawable.importForegroundPlanet(planetAnimator.planet, planetAnimator.robot)
            } else {
                plotter.drawable = editPlanetDrawable
            }

            if (animateProperty.value) {
                animationTimer.start()
            } else {
                animationTimer.stop()
            }
        }
        editableProperty.onChange {
            if (editableProperty.value) {
                animateProperty.value = false
            }
        }

        editPlanetDrawable.editCallback = planetFile


        animationTimer.onRender {
            planetAnimator.update()
            livePlanetDrawable.importForegroundPlanet(planetAnimator.planet, planetAnimator.robot)
        }

        planetFile.history.valueProperty.onChange {
            editPlanetDrawable.importPlanet(planetFile.planet.value)
            livePlanetDrawable.importBackgroundPlanet(planetFile.planet.value)
        }

        editPlanetDrawable.importPlanet(planetFile.planet.value)
        livePlanetDrawable.importBackgroundPlanet(planetFile.planet.value)
        livePlanetDrawable.importForegroundPlanet(planetAnimator.planet, planetAnimator.robot)
    }

    fun exportSVG(): String {
        val dimension = exportGetSize()
        val canvas = SvgCanvas(dimension.width, dimension.height)

        exportRender(canvas)

        return canvas.buildFile()
    }

    fun exportGetSize(): Dimension {
        val rect = BackgroundDrawable.calcPlanetArea(planetFile.planet.value)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }

    fun exportRender(canvas: ICanvas) {
        val drawable = PlanetDrawable(drawCompass = false, drawName = true)
        drawable.importPlanet(planetFile.planet.value)

        val plotter = ExportPlotter(canvas, drawable)

        drawable.centerPlanet()

        plotter.render(0.0)
    }

    companion object {
        const val ANIMATION_TIME = 1000.0
    }

    enum class Theme(val theme: ITheme) {
        LIGHT(LightTheme),
        DARK(DarkTheme)
    }
}
