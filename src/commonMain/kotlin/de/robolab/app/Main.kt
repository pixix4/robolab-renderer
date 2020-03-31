package de.robolab.app

import de.robolab.file.PlanetFile
import de.robolab.file.demoFile
import de.robolab.file.toFixed
import de.robolab.model.Coordinate
import de.robolab.model.Path
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.ExportPlotter
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.BackgroundDrawable
import de.robolab.renderer.drawable.PlanetDrawable
import de.robolab.renderer.drawable.edit.*
import de.robolab.renderer.platform.CommonTimer
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.theme.DarkTheme
import de.robolab.renderer.theme.ITheme
import de.robolab.renderer.theme.LightTheme
import de.robolab.renderer.utils.Transformation
import de.robolab.svg.SvgCanvas
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.roundToInt

class Main(val canvas: ICanvas) {


    private val timer = CommonTimer(50.0)
    private val plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)
    private val planetDrawable = EditPlanetDrawable()

    private val animationTimer = CommonTimer(1000 / (ANIMATION_TIME * 1.25))

    val animateProperty = property(false)
    val editableProperty = planetDrawable.editableProperty
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

    val themeProperty = property(Theme.LIGHT)
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
        plotter.drawable = planetDrawable
        plotter.theme = themeProperty.value.theme
        themeProperty.onChange {
            plotter.theme = themeProperty.value.theme
        }

        animateProperty.onChange {
            if (animateProperty.value) {
                editableProperty.value = false
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

        planetDrawable.editCallback = planetFile

        var isUndoPhase = false

        animationTimer.onRender {
            if (isUndoPhase && !planetFile.history.canUndo) {
                isUndoPhase = false
            } else if (!isUndoPhase && !planetFile.history.canRedo) {
                isUndoPhase = true
            }
            if (isUndoPhase) {
                planetFile.history.undo()
            } else {
                planetFile.history.redo()
            }
        }

        planetFile.history.valueProperty.onChange {
            planetDrawable.importPlanet(planetFile.planet.value)
        }

        planetDrawable.importPlanet(planetFile.planet.value)
    }

    fun exportSVG(): String? {
        val planet = planetFile.planet.value
        val rect = BackgroundDrawable.calcPlanetArea(planet)?.expand(0.99) ?: return null

        val drawable = PlanetDrawable(drawCompass = false, drawName = true)
        drawable.importPlanet(planet)

        val canvas = SvgCanvas(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
        val plotter = ExportPlotter(canvas, drawable)

        drawable.centerPlanet()

        plotter.render(0.0)
        return canvas.buildFile()
    }

    companion object {
        const val ANIMATION_TIME = 1000.0
    }

    enum class Theme(val theme: ITheme) {
        LIGHT(LightTheme),
        DARK(DarkTheme)
    }
}