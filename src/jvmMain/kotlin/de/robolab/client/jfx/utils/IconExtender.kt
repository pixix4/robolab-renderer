package de.robolab.client.jfx.utils

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import de.robolab.client.jfx.style.MainStyle
import javafx.beans.value.ObservableValue
import javafx.scene.Parent
import javafx.scene.paint.Color
import tornadofx.addClass
import tornadofx.css
import tornadofx.onChange
import tornadofx.opcr

/**
 * @author lars
 */

fun iconNoAdd(icon: MaterialIcon, op: MaterialIconView.() -> Unit = {}) =
    MaterialIconView(icon).also(op).also {
        it.addClass(MainStyle.iconView)
    }

fun iconNoAdd(icon: ObservableValue<MaterialIcon>, op: MaterialIconView.() -> Unit = {}): MaterialIconView {
    val view = MaterialIconView(icon.value).also(op)
    icon.onChange {
        view.setIcon(it)
    }
    return view.also {
        it.addClass(MainStyle.iconView)
    }
}

fun Parent.icon(icon: MaterialIcon, op: MaterialIconView.() -> Unit = {}) =
    opcr(this, MaterialIconView(icon), op).also {
        it.addClass(MainStyle.iconView)
    }

fun Parent.icon(icon: ObservableValue<MaterialIcon>, op: MaterialIconView.() -> Unit = {}): MaterialIconView {
    val view = opcr(this, MaterialIconView(icon.value), op)
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
