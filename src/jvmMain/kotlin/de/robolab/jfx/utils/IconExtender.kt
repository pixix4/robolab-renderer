package de.robolab.jfx.utils

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.paint.Color
import tornadofx.*

/**
 * @author lars
 */

fun iconNoAdd(icon: FontAwesomeIcon, op: FontAwesomeIconView.() -> Unit = {}) =
        FontAwesomeIconView(icon).also(op)

fun iconNoAdd(icon: MaterialIcon, op: MaterialIconView.() -> Unit = {}) =
        MaterialIconView(icon).also(op)

fun iconNoAdd(icon: ObservableValue<FontAwesomeIcon>, op: FontAwesomeIconView.() -> Unit = {}): FontAwesomeIconView {
    val view = FontAwesomeIconView(icon.value).also(op)
    icon.onChange {
        view.setIcon(it)
    }
    return view
}

fun iconNoAdd(icon: ObservableValue<MaterialIcon>, op: MaterialIconView.() -> Unit = {}): MaterialIconView {
    val view = MaterialIconView(icon.value).also(op)
    icon.onChange {
        view.setIcon(it)
    }
    return view
}

fun Parent.icon(icon: FontAwesomeIcon, op: FontAwesomeIconView.() -> Unit = {}) =
        opcr(this, FontAwesomeIconView(icon), op)

fun Parent.icon(icon: MaterialIcon, op: MaterialIconView.() -> Unit = {}) =
        opcr(this, MaterialIconView(icon), op)

fun Parent.icon(icon: ObservableValue<FontAwesomeIcon>, op: FontAwesomeIconView.() -> Unit = {}): FontAwesomeIconView {
    val view = opcr(this, FontAwesomeIconView(icon.value), op)
    icon.onChange {
        view.setIcon(it)
    }
    return view
}

fun Parent.icon(icon: ObservableValue<MaterialIcon>, op: MaterialIconView.() -> Unit = {}): MaterialIconView {
    val view = opcr(this, MaterialIconView(icon.value), op)
    icon.onChange {
        view.setIcon(it)
    }
    return view
}

@JvmName("fontAwesomeIconButton")
inline fun EventTarget.iconButton(
        icon: ObservableValue<FontAwesomeIcon>,
        iconSize: Dimension<Dimension.LinearUnits> = 16.pt,
        crossinline init: Button.(FontAwesomeIconView) -> Unit = {}
) =
        button {
            icon(icon) {
                glyphSize = iconSize.value
                init(this@button, this)
            }
        }

fun FontAwesomeIconView.setIconColor(color: Color) {
    glyphStyle = "-fx-fill: ${color.css};"
}

@JvmName("materialIconButton")
inline fun EventTarget.iconButton(
        icon: ObservableValue<MaterialIcon>,
        iconSize: Dimension<Dimension.LinearUnits> = 16.pt,
        crossinline init: Button.(MaterialIconView) -> Unit = {}
) =
        button {
            icon(icon) {
                glyphSize = iconSize.value
                init(this@button, this)
            }
        }

fun MaterialIconView.setIconColor(color: Color) {
    glyphStyle = "-fx-fill: ${color.css};"
}
