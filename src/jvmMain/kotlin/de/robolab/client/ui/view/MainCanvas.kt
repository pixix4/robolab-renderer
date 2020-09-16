package de.robolab.client.ui.view

import de.robolab.client.app.controller.CanvasController
import de.robolab.client.app.controller.UiController
import de.robolab.client.ui.adapter.FxCanvas
import javafx.scene.Cursor
import javafx.scene.layout.Priority
import tornadofx.*

class MainCanvas(
    canvasController: CanvasController,
    private val uiController: UiController,
) : View() {

    private val canvas = FxCanvas()

    override val root = anchorpane {
        minWidth = 0.0
        minHeight = 0.0

        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        add(canvas.canvas)
        canvas.canvas.widthProperty().bind(widthProperty())
        canvas.canvas.heightProperty().bind(heightProperty())

        canvas.canvas.anchorpaneConstraints {
            topAnchor = 0.0
            leftAnchor = 0.0
            rightAnchor = 0.0
            bottomAnchor = 0.0
        }

        hbox {
            anchorpaneConstraints {
                topAnchor = 0.0
                leftAnchor = 0.0
                bottomAnchor = 0.0
            }

            minWidth = 10.0
            prefWidth = 10.0
            maxWidth = 10.0

            style {
                cursor = Cursor.W_RESIZE
            }

            setOnMouseDragged { event ->
                if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
                    uiController.setNavigationBarWidth(event.screenX - scene.window.x)
                }
            }
        }

        hbox {
            anchorpaneConstraints {
                topAnchor = 0.0
                rightAnchor = 0.0
                bottomAnchor = 0.0
            }

            minWidth = 10.0
            prefWidth = 10.0
            maxWidth = 10.0

            style {
                cursor = Cursor.E_RESIZE
            }

            setOnMouseDragged { event ->
                if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
                    uiController.setInfoBarWidth((scene.window.x + scene.window.width) - event.screenX)
                }
            }
        }
    }


    init {
        canvasController.setupCanvas(canvas)
    }
}
