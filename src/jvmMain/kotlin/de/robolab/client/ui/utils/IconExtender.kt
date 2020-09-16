package de.robolab.client.ui.utils

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import de.robolab.client.ui.style.MainStyle
import javafx.beans.value.ObservableValue
import javafx.scene.Parent
import javafx.scene.control.Labeled
import javafx.scene.paint.Color
import tornadofx.addClass
import tornadofx.css
import tornadofx.onChange
import tornadofx.opcr

/**
 * @author lars
 */

fun iconNoAdd(icon: MaterialIcon, size: String = "1.2em", op: MaterialIconView.() -> Unit = {}) =
    MaterialIconView(icon, size).also(op).also {
        it.addClass(MainStyle.iconView)
    }

fun Labeled.setIcon(icon: MaterialIcon, size: String = "1.2em") {
    graphic = MaterialIconView(icon, size).also {
        it.addClass(MainStyle.iconView)
    }
}

fun iconNoAdd(icon: ObservableValue<MaterialIcon>, size: String = "1.2em", op: MaterialIconView.() -> Unit = {}): MaterialIconView {
    val view = MaterialIconView(icon.value, size).also(op)
    icon.onChange {
        view.setIcon(it)
    }
    return view.also {
        it.addClass(MainStyle.iconView)
    }
}

fun Parent.icon(icon: MaterialIcon, size: String = "1.2em", op: MaterialIconView.() -> Unit = {}) =
    opcr(this, MaterialIconView(icon, size), op).also {
        it.addClass(MainStyle.iconView)
    }

fun Parent.icon(icon: ObservableValue<MaterialIcon>, size: String = "1.2em", op: MaterialIconView.() -> Unit = {}): MaterialIconView {
    val view = opcr(this, MaterialIconView(icon.value, size), op)
    icon.onChange {
        view.setIcon(it)
    }
    return view.also {
        it.addClass(MainStyle.iconView)
    }
}

fun MaterialIconView.setIconColor(color: Color) {
    glyphStyle = "-fx-fill: ${color.css};"
}
