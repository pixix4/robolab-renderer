package de.roboplot.plotter.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.roboplot.plotter.flatBinding
import de.roboplot.plotter.model.PathClassifier
import de.roboplot.plotter.model.Point
import de.roboplot.plotter.nonNullObjectBinding
import de.roboplot.plotter.plotter.Exporter
import de.roboplot.plotter.plotter.Plotter
import de.roboplot.plotter.util.Configuration
import de.roboplot.plotter.util.DragResizeMod
import de.roboplot.plotter.view.viewmodel.FilePlanetViewModel
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.util.StringConverter
import tornadofx.*

class PlanetSideBar : Fragment() {

    private val viewModel: FilePlanetViewModel by inject()

    private val modeGroup = ToggleGroup()

    private val viewModeToggle: ToggleButton = togglebutton("", selectFirst = true, group = modeGroup) {
        icon(MaterialIcon.VISIBILITY)
        font = Font(16.0)
        tooltip("View mode")
        setOnAction { }
    }
    private val editModeToggle: ToggleButton = togglebutton("", selectFirst = false, group = modeGroup) {
        icon(MaterialIcon.ADD)
        font = Font(16.0)
        tooltip("Planet edit mode")
    }
    private val splineModeToggle: ToggleButton = togglebutton("", selectFirst = false, group = modeGroup) {
        icon(MaterialIcon.EDIT)
        font = Font(16.0)
        tooltip("Spline edit mode")
    }

    private lateinit var statisticsView: TextArea

    override val root = vbox {
        prefWidth = SideBar.PREF_WIDTH
        toolbar {
            button("Save") {
                icon(MaterialIcon.SAVE) {
                    glyphSize = 16
                }
                font = Font(16.0)
                action(viewModel::save)

                visibleWhen(Configuration.General.autosaveProperty.nonNullObjectBinding { !value })
                shortcut(KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN))
            }
            spacer()

            add(viewModeToggle)
            add(editModeToggle)
            add(splineModeToggle)

            menubutton {
                item("Format", KeyCodeCombination(
                        KeyCode.L,
                        KeyCombination.SHORTCUT_DOWN,
                        KeyCombination.ALT_DOWN
                )) {
                    action {
                        viewModel.format()
                    }
                }
                item("Translate") {
                    action {
                        val start = viewModel.planet.start

                        val xTranslation = SimpleIntegerProperty(0)
                        val yTranslation = SimpleIntegerProperty(0)

                        var allow = false

                        val typeGroup = togglegroup {
                            selectedToggleProperty().onChange {
                                if (it != null && start != null && allow) {
                                    if (it.userData == "relative") {
                                        xTranslation -= start.x
                                        yTranslation -= start.y
                                    } else {
                                        xTranslation += start.x
                                        yTranslation += start.y
                                    }
                                } else {
                                    allow = true
                                }
                            }
                        }

                        val minusStrConverter = object : StringConverter<Number>() {
                            override fun toString(i: Number?): String {
                                return i.toString()
                            }

                            override fun fromString(s: String?): Number {
                                return s?.toIntOrNull() ?: 0
                            }

                        }

                        dialog("Translate") {
                            vbox {
                                label("Translation type:")
                                hbox {
                                    radiobutton("Relative", typeGroup) {
                                        userData = "relative"
                                        isSelected = true
                                    }
                                    if (start != null) {
                                        radiobutton("Absolute to start", typeGroup) {
                                            userData = "start"
                                        }
                                    }
                                }
                            }
                            hbox {
                                label("x")
                                textfield(xTranslation, minusStrConverter) {
                                    filterInput {
                                        it.controlNewText.isInt() || it.controlNewText == "-"
                                    }
                                }

                            }
                            hbox {
                                label("y")
                                textfield(yTranslation, minusStrConverter) {
                                    filterInput {
                                        it.controlNewText.isInt() || it.controlNewText == "-"
                                    }

                                }
                            }
                            hbox {
                                button("Apply") {
                                    action {
                                        if (typeGroup.selectedToggle.userData != "relative" && start != null) {
                                            xTranslation -= start.x
                                            yTranslation -= start.y
                                        }
                                        viewModel.translateBy(Point(xTranslation.value, yTranslation.value))
                                        this@dialog.close()
                                    }
                                }
                                button("Abort") {
                                    action {
                                        this@dialog.close()
                                    }
                                }
                            }
                        }
                    }
                }
                item("Rotate clockwise") {
                    action {
                        viewModel.rotateClockwise()
                    }
                }
                item("Rotate counter-clockwise") {
                    action {
                        viewModel.rotateCounterClockwise()
                    }
                }
                item("Scale weights") {
                    action {
                        val scale = SimpleDoubleProperty(1.0)
                        dialog("Scale weights") {
                            hbox {
                                label("Scale")
                                textfield(scale) {
                                    filterInput { it.controlNewText.replace(",", ".").isDouble() }
                                }
                            }
                            hbox {
                                button("Apply") {
                                    action {
                                        viewModel.scaleWeights(scale.value)
                                        this@dialog.close()
                                    }
                                }
                                button("Abort") {
                                    action {
                                        this@dialog.close()
                                    }
                                }
                            }
                        }
                    }
                }
                item("Export") {
                    action {
                        Exporter.export(viewModel.planet, this@PlanetSideBar)
                    }
                }
                item("Traverse") {
                    action {
                        println("Starting new traversal of '${viewModel.planet.name}'")
                        de.roboplot.plotter.traverser.DefaultTraverser(viewModel.planet, true)
                                .filter { it.status != de.roboplot.plotter.traverser.TraverserState.Status.Running }
                                .forEach {
                                    println(it.getTrail())
                                }
                    }
                }
            }
        }

        add<PlanetTextEditor>()

        vbox {
            minHeight = 0.0
            prefHeight = 300.0
            maxHeight = 500.0


            statisticsView = textarea {
                vgrow = Priority.ALWAYS
                isEditable = false

                style {
                    fontFamily = "monospace"
                }
            }

            drawStatistics()


            DragResizeMod.makeResizable(this, setOf(DragResizeMod.Allow.RESIZE_TOP), object : DragResizeMod.OnDragResizeEventListener {
                override fun onResize(node: Node, x: Double, y: Double, h: Double, w: Double) {
                    @Suppress("NAME_SHADOWING") var h = h
                    if (h < minHeight) {
                        h = minHeight
                    }
                    if (h > maxHeight) {
                        h = maxHeight
                    }
                    prefHeight = h
                }
            })
        }
    }

    private fun drawStatistics() = with(statisticsView) {
        clear()
        val planet = viewModel.planet

        val data = mutableListOf(
                "Points" to "",
                "Point count" to planet.statistic.pointCount.toString(),
                "Blue point count" to planet.statistic.pointBlueCount.toString(),
                "Red point count" to planet.statistic.pointRedCount.toString(),
                "" to "",
                "Paths" to "",
                "Path count" to planet.statistic.pathCount.toString(),
                "Free path count" to planet.statistic.pathFreeCount.toString(),
                "Blocked path count" to planet.statistic.pathBlockedCount.toString(),
                "" to "",
                "Path classifiers" to "")
        for (classifier in PathClassifier.values()) {
            val count = planet.statistic.pathClassifier.getOrDefault(classifier, 0)
            data += classifier.desc to count.toString()
        }

        val length = data.map { it.first.length }.max() ?: 0

        val builder = StringBuilder()
        for ((key, value) in data) {
            if (value.isEmpty()) {
                builder.append(key)
            } else {
                builder.append("$key: ${" ".repeat(length - key.length)}")
                builder.append(value)
            }
            builder.append("\n")
        }

        text = builder.toString()
    }

    private val editModeProperty = viewModel.itemProperty.flatBinding { it.editModeProperty }

    init {
        viewModel.planetProperty.onChange {
            drawStatistics()
        }
        editModeProperty.onChange {
            val toggle = when (it) {
                Plotter.EditMode.DEFAULT -> viewModeToggle
                Plotter.EditMode.PLANET -> editModeToggle
                Plotter.EditMode.PATH -> splineModeToggle
                null -> viewModeToggle
            }
            if (modeGroup.selectedToggle != toggle) {
                modeGroup.selectToggle(toggle)
            }
        }

        modeGroup.selectedToggleProperty().onChange {
            when (it) {
                viewModeToggle -> viewModel.item.editModeProperty.value = Plotter.EditMode.DEFAULT
                editModeToggle -> viewModel.item.editModeProperty.value = Plotter.EditMode.PLANET
                splineModeToggle -> viewModel.item.editModeProperty.value = Plotter.EditMode.PATH
                else -> modeGroup.selectToggle(when (editModeProperty.value) {
                    Plotter.EditMode.DEFAULT, null -> viewModeToggle
                    Plotter.EditMode.PLANET -> editModeToggle
                    Plotter.EditMode.PATH -> splineModeToggle
                })
            }
        }

        modeGroup.selectToggle(viewModeToggle)
    }
}
