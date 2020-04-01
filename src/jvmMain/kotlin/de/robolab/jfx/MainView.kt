package de.robolab.jfx

import de.robolab.app.Main
import de.robolab.jfx.adapter.AwtCanvas
import de.robolab.jfx.adapter.FxCanvas
import de.robolab.jfx.adapter.toProperty
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.property.mapBinding
import javafx.application.Platform
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File
import java.time.LocalDate
import kotlin.system.exitProcess

class MainView : View() {

    override val root: BorderPane = borderpane {
        title = headerText

        Platform.runLater {
            requestFocus()
        }

        val canvas = FxCanvas()
        val main = Main(canvas)

        center {
            vbox {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                toolbar {
                    val toggleGroup = ToggleGroup()
                    togglebutton("Animate", toggleGroup, false) {
                        selectedProperty().toProperty().bindBidirectional(main.animateProperty)
                    }
                    togglebutton("Editable", toggleGroup, false) {
                        selectedProperty().toProperty().bindBidirectional(main.editableProperty)
                    }
                    separator()
                    val themeGroup = ToggleGroup()
                    togglebutton("Light theme", themeGroup) {
                        selectedProperty().toProperty().bindBidirectional(main.lightThemeProperty)
                        selectedProperty().onChange {
                            isSelected = main.lightThemeProperty.value
                        }
                    }
                    togglebutton("Dark theme", themeGroup) {
                        selectedProperty().toProperty().bindBidirectional(main.darkThemeProperty)
                        selectedProperty().onChange {
                            isSelected = main.darkThemeProperty.value
                        }
                    }
                    separator()
                    button("Export SVG") {
                        setOnAction {
                            File("export.svg").writeText(main.exportSVG())
                        }
                    }
                    button("Export PNG") {
                        setOnAction {
                            val dimension = main.exportGetSize()
                            val exportCanvas = AwtCanvas(dimension.width, dimension.height, PreferenceStorage.exportScale)

                            main.exportRender(exportCanvas)

                            exportCanvas.writePNG(File("export.png"))
                        }
                    }
                }

                hbox {
                    vgrow = Priority.ALWAYS
                    hgrow = Priority.ALWAYS

                    add(canvas.canvas)
                    canvas.canvas.widthProperty().bind(widthProperty())
                    canvas.canvas.heightProperty().bind(heightProperty())
                }

                hbox {
                    label {
                        textProperty().toProperty().bind(main.pointerProperty.mapBinding { it.joinToString(" | ") })
                    }
                }
            }
        }
    }

    override fun onUndock() {
        exitProcess(0)
    }

    companion object {
        val headerText: String = "RoboLab ${LocalDate.now().year}"
    }
}
