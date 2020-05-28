package de.robolab.client.jfx.dialog

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.client.jfx.style.MainStyle
import de.robolab.client.jfx.utils.iconNoAdd
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import javafx.stage.StageStyle
import javafx.util.StringConverter
import tornadofx.*

abstract class GenericDialog : View() {


    protected fun buildContent(name: String, init: VBox.() -> Unit): VBox {
        val dialog = VBox()
        dialog.addClass(MainStyle.dialog)

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

        dialog.scrollpane(fitToWidth = true, fitToHeight = false) {
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED

            vbox {
                style {
                    padding = box(1.em, 1.em, 0.em, 1.em)
                }

                init()
            }
        }

        return dialog
    }

    companion object {
        inline fun <reified D : GenericDialog> open(vararg params: Pair<String, Any?>) {
            find(D::class, FX.defaultScope, params.toMap()).openModal(StageStyle.UTILITY, resizable = false)
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
