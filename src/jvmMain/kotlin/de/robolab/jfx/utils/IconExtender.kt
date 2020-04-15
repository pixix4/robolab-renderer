package de.robolab.jfx.utils

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import de.robolab.jfx.style.MainStyle
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
        FontAwesomeIconView(icon).also(op).also {
            it.addClass(MainStyle.iconView)
        }

fun iconNoAdd(icon: MaterialIcon, op: MaterialIconView.() -> Unit = {}) =
        MaterialIconView(icon).also(op).also {
            it.addClass(MainStyle.iconView)
        }

fun iconNoAdd(icon: ObservableValue<FontAwesomeIcon>, op: FontAwesomeIconView.() -> Unit = {}): FontAwesomeIconView {
    val view = FontAwesomeIconView(icon.value).also(op)
    icon.onChange {
        view.setIcon(it)
    }
    return view.also {
        it.addClass(MainStyle.iconView)
    }
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

fun Parent.icon(icon: FontAwesomeIcon, op: FontAwesomeIconView.() -> Unit = {}) =
        opcr(this, FontAwesomeIconView(icon), op).also {
            it.addClass(MainStyle.iconView)
        }

fun Parent.icon(icon: MaterialIcon, op: MaterialIconView.() -> Unit = {}) =
        opcr(this, MaterialIconView(icon), op).also {
            it.addClass(MainStyle.iconView)
        }

fun Parent.icon(icon: ObservableValue<FontAwesomeIcon>, op: FontAwesomeIconView.() -> Unit = {}): FontAwesomeIconView {
    val view = opcr(this, FontAwesomeIconView(icon.value), op)
    icon.onChange {
        view.setIcon(it)
    }
    return view.also {
        it.addClass(MainStyle.iconView)
    }
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

fun FontAwesomeIconView.setIconColor(color: Color) {
    glyphStyle = "-fx-fill: ${color.css};"
}

fun MaterialIconView.setIconColor(color: Color) {
    glyphStyle = "-fx-fill: ${color.css};"
}
