package de.robolab.client.ui.dialog

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.client.app.controller.MainController
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.ui.utils.iconNoAdd
import javafx.scene.control.ScrollPane
import javafx.scene.control.TabPane
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.StageStyle
import javafx.util.StringConverter
import tornadofx.*

abstract class GenericDialog : View() {

    private val tabList = mutableListOf<Pair<String, VBox.() -> Unit>>()
    protected fun tab(name: String = "", init: VBox.() -> Unit) {
        tabList += name to init
    }

    private fun VBox.initSingleTab() {
        val (_, init) = tabList.single()

        scrollpane(fitToWidth = true, fitToHeight = false) {
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED

            vbox {
                style {
                    padding = box(1.em, 1.em, 0.em, 1.em)
                }

                init()
            }
        }
    }

    private fun VBox.initMultiTab() {
        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            for ((name, init) in tabList) {
                tab(name) {
                    scrollpane(fitToWidth = true, fitToHeight = false) {
                        hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                        vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED

                        vbox {
                            style {
                                padding = box(1.em, 1.em, 0.em, 1.em)
                            }

                            init()
                        }
                    }
                }
            }
        }
    }


    protected fun buildContent(name: String, init: VBox.() -> Unit): VBox {
        tab(init = init)
        return buildContent(name)
    }

    protected fun buildContent(name: String): VBox {
        val dialog = VBox()
        dialog.addClass(MainStyle.dialog)
        dialog.maxWidth = 640.0

        title = "${MainController.APPLICATION_NAME} - $name"
        dialog.hbox {
            addClass(MainStyle.toolBar)

            style {
                padding = box(0.6.em, 1.5.em, 0.6.em, 1.5.em)
            }

            label(name) {
                style {
                    fontWeight = FontWeight.BOLD
                    fontSize = 1.2.em
                }
            }
            spacer()
            button {
                graphic = iconNoAdd(MaterialIcon.CLOSE)
                de.robolab.client.utils.runAsync {
                    requestFocus()
                }

                setOnAction {
                    close()
                }
            }

            var initialX = 0.0
            var initialY = 0.0
            setOnMousePressed { me ->
                if (me.button !== javafx.scene.input.MouseButton.MIDDLE) {
                    initialX = me.sceneX
                    initialY = me.sceneY
                }
            }
            setOnMouseDragged { me ->
                if (me.button !== javafx.scene.input.MouseButton.MIDDLE) {
                    scene.window.x = me.screenX - initialX
                    scene.window.y = me.screenY - initialY
                }
            }
        }

        when {
            tabList.size <= 0 -> {
                // Nothing to do
            }
            tabList.size == 1 -> {
                dialog.initSingleTab()
            }
            else -> {
                dialog.initMultiTab()
            }
        }

        return dialog
    }

    companion object {
        inline fun <reified D : GenericDialog> open(vararg params: Pair<String, Any?>) {
            find(D::class, FX.defaultScope, params.toMap())
                .openModal(StageStyle.UTILITY, modality = Modality.APPLICATION_MODAL, resizable = false)
                ?.toFront()
        }
    }
}


class DoubleStringConverter(private val default: Double) : StringConverter<Double>() {
    override fun toString(obj: Double?): String {
        return obj?.toString() ?: default.toString()
    }

    override fun fromString(string: String?): Double {
        return string?.toDoubleOrNull() ?: default
    }
}

class IntStringConverter(private val default: Int) : StringConverter<Int>() {
    override fun toString(obj: Int?): String {
        return obj?.toString() ?: default.toString()
    }

    override fun fromString(string: String?): Int {
        return string?.toIntOrNull() ?: default
    }
}

class NullableIntStringConverter : StringConverter<Int?>() {
    override fun toString(obj: Int?): String {
        return obj?.toString() ?: ""
    }

    override fun fromString(string: String?): Int? {
        return string?.toIntOrNull()
    }
}

class NullableStringConverter : StringConverter<String?>() {
    override fun toString(obj: String?): String {
        return obj ?: ""
    }

    override fun fromString(string: String?): String? {
        return if (string?.isEmpty() == true) null else string
    }
}

