package de.robolab.client.ui.view

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.ui.style.MainStyle
import javafx.beans.Observable
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.css.*
import javafx.scene.Parent
import javafx.scene.control.Labeled
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class MaterialIconView(icon: MaterialIcon, iconSize: String = "1em") : Text() {

    var glyphStyle: StringProperty? = null
    private var glyphFontFamily: String? = null
        private set
    private var unicode: String? = null
    private var glyphName: ObjectProperty<String>? = null
    private var glyphSize: ObjectProperty<Number>? = null

    private fun initProperties() {
        styleClass.addAll("glyph-icon")
        glyphSizeProperty().addListener { _: Observable? -> updateSize() }
        glyphStyleProperty().addListener { _: Observable? -> updateStyle() }
        glyphNameProperty().addListener { _: Observable? -> updateIcon() }
        setIcon(defaultGlyph)
    }

    private fun glyphStyleProperty(): StringProperty {
        val style = glyphStyle
        return if (style == null) {
            val h = SimpleStringProperty("")
            glyphStyle = h
            h
        } else {
            style
        }
    }

    private fun getGlyphStyle(): String {
        return glyphStyleProperty().value
    }

    private fun setGlyphStyle(style: String) {
        var s = style
        if (getGlyphStyle().isNotEmpty() && !getGlyphStyle().endsWith(";")) {
            s = ";$s"
        }
        glyphStyleProperty().value = getGlyphStyle() + s
    }

    fun glyphNameProperty(): ObjectProperty<String> {
        val name = glyphName
        return if (name == null) {
            val h = SimpleStyleableObjectProperty(StyleableProperties.GLYPH_NAME, this, "glyphName")
            glyphName = h
            return h
        } else name
    }

    private fun getGlyphName(): String {
        return glyphNameProperty().value
    }

    private fun setGlyphName(glyphName: String) {
        glyphNameProperty().value = glyphName
    }

    fun glyphSizeProperty(): ObjectProperty<Number> {
        val size = glyphSize
        return if (size == null) {
            val h = SimpleStyleableObjectProperty(StyleableProperties.GLYPH_SIZE, this, "glyphSize")
            h.value = DEFAULT_ICON_SIZE
            glyphSize = h
            h
        } else size
    }

    private fun getGlyphSize(): Number {
        return glyphSizeProperty().value
    }

    private fun setGlyphSize(size: Number?) {
        var s = size
        s = s ?: DEFAULT_ICON_SIZE
        glyphSizeProperty().value = s
    }

    var size: String?
        get() = getGlyphSize().toString()
        set(sizeExpr) {
            val s = convert(sizeExpr)
            setGlyphSize(s)
        }

    fun setIcon(glyph: MaterialIcon) {
        setGlyphName(glyph.name)
        glyphFontFamily = "\'Material Icons\'"
        unicode = glyph.unicode
    }

    private val defaultGlyph = MaterialIcon.ANDROID

    private fun updateSize() {
        val f = Font(font.family, getGlyphSize().toDouble())
        font = f
        setGlyphStyle(
            String.format(
                "-fx-font-family: %s; -fx-font-size: %s;",
                glyphFontFamily,
                getGlyphSize().toDouble()
            )
        )
    }

    private fun updateIcon() {
        var icon: MaterialIcon = defaultGlyph
        try {
            icon = MaterialIcon.valueOf(getGlyphName())
        } catch (e: Exception) {
            val msg = String.format(
                "Icon '%s' not found. Using '%s' (default) instead", getGlyphName(),
                defaultGlyph
            )
            Logger.getLogger(MaterialIcon::class.java.name).log(Level.SEVERE, msg)
        }
        text = icon.unicode
    }

    private fun updateStyle() {
        style = getGlyphStyle()
    }

    init {
        initProperties()
        setIcon(icon)
        style = String.format("-fx-font-family: 'Material Icons'; -fx-font-size: %s;", iconSize)
    }

    // CSS
    @Suppress("UNCHECKED_CAST")
    private object StyleableProperties {
        val GLYPH_NAME: CssMetaData<MaterialIconView, String> =
            object :
                CssMetaData<MaterialIconView, String>("-glyph-name", StyleConverter.getStringConverter(), "BLANK") {
                override fun isSettable(styleable: MaterialIconView): Boolean {
                    val name = styleable.glyphName
                    return name == null || !name.isBound
                }

                override fun getStyleableProperty(styleable: MaterialIconView): StyleableProperty<String> {
                    return styleable.glyphNameProperty() as StyleableProperty<String>
                }

                override fun getInitialValue(styleable: MaterialIconView): String {
                    return "BLANK"
                }
            }
        val GLYPH_SIZE: CssMetaData<MaterialIconView, Number> = object : CssMetaData<MaterialIconView, Number>(
            "-glyph-size",
            StyleConverter.getSizeConverter(),
            DEFAULT_ICON_SIZE
        ) {
            override fun isSettable(styleable: MaterialIconView): Boolean {
                val size = styleable.glyphSize
                return size == null || !size.isBound
            }

            override fun getStyleableProperty(styleable: MaterialIconView): StyleableProperty<Number> {
                return styleable.glyphSizeProperty() as StyleableProperty<Number>
            }

            override fun getInitialValue(styleable: MaterialIconView): Number {
                return DEFAULT_ICON_SIZE
            }
        }

        val STYLEABLES: List<CssMetaData<out Styleable?, *>>

        init {
            val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(
                getClassCssMetaData()
            )
            Collections.addAll(
                styleables,
                GLYPH_NAME,
                GLYPH_SIZE
            )
            STYLEABLES = Collections.unmodifiableList(styleables)
        }
    }

    override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>> {
        return classCssMetaData
    }

    fun convert(sizeString: String?): Number {
        return convert(sizeString ?: "", font)
    }

    companion object {
        const val DEFAULT_ICON_SIZE = 12.0

        val classCssMetaData: List<CssMetaData<out Styleable?, *>>
            get() = StyleableProperties.STYLEABLES


        private val CSS_PARSER = CssParser()
        private val DEFAULT_SIZE: Number = 12.0
        fun convert(sizeString: String, font: Font?): Number {
            val stylesheet = CSS_PARSER.parse("{-fx-font-size: $sizeString;}")
            if (stylesheet.rules.isEmpty()) {
                return DEFAULT_SIZE
            } else if (stylesheet.rules[0].declarations.isEmpty()) {
                return DEFAULT_SIZE
            }
            return stylesheet.rules[0].declarations[0].parsedValue.convert(font) as Number
        }

        init {
            try {
                loadFont("/Roboto/MaterialIcons-Regular.ttf", 10.0)
            } catch (ex: IOException) {
                Logger.getLogger(MaterialIconView::class.simpleName).log(Level.SEVERE, null, ex)
            }
        }
    }
}


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
        view.setIcon(it ?: MaterialIcon.ANDROID)
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
        view.setIcon(it ?: MaterialIcon.ANDROID)
    }
    return view.also {
        it.addClass(MainStyle.iconView)
    }
}

fun MaterialIconView.setIconColor(color: Color) {
    glyphStyle?.value = "-fx-fill: ${color.css};"
}
