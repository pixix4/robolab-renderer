package de.robolab.jfx

import de.robolab.app.Main
import de.robolab.jfx.adapter.FxCanvas
import de.robolab.jfx.adapter.toProperty
import javafx.application.Platform
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import tornadofx.*
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
                        textProperty().toProperty().bind(main.pointerProperty)
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
