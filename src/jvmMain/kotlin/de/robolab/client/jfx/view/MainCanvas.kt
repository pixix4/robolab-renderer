package de.robolab.client.jfx.view

import de.robolab.client.app.controller.CanvasController
import de.robolab.client.jfx.adapter.FxCanvas
import de.westermann.kobserve.base.ObservableProperty
import javafx.scene.Cursor
import tornadofx.*
import kotlin.math.max

class MainCanvas(
    canvasController: CanvasController,
    navigationBarActiveProperty: ObservableProperty<Boolean>,
    navigationBarWidthProperty: ObservableProperty<Double>,
    infoBarActiveProperty: ObservableProperty<Boolean>,
    infoBarWidthProperty: ObservableProperty<Double>
) : View() {

    private val canvas = FxCanvas()

    override val root = anchorpane {
        minWidth = 0.0
        minHeight = 0.0

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
                    var width = event.screenX - scene.window.x
                    if (navigationBarActiveProperty.value) {
                        if (width < 50.0) {
                            navigationBarActiveProperty.value = false
                        } else {
                            width = max(width, 200.0)
                            navigationBarWidthProperty.value = width
                        }
                    } else {
                        if (width >= 50.0) {
                            navigationBarActiveProperty.value = true
                        }
                    }
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
                    var width = (scene.window.x + scene.window.width) - event.screenX
                    if (infoBarActiveProperty.value) {
                        if (width < 50.0) {
                            infoBarActiveProperty.value = false
                        } else {
                            width = max(width, 200.0)
                            infoBarWidthProperty.value = width
                        }
                    } else {
                        if (width >= 50.0) {
                            infoBarActiveProperty.value = true
                        }
                    }
                }
            }
        }
    }

    init {
        canvasController.setupCanvas(canvas)
    }
}
