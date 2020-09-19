package de.robolab.client.ui.view.boxes

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.InfoBarFileEdit
import de.robolab.client.app.model.file.PathDetailBox
import de.robolab.client.app.model.file.PlanetStatisticsDetailBox
import de.robolab.client.app.model.file.PointDetailBox
import de.robolab.client.renderer.view.base.ActionHint
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.ui.view.icon
import de.robolab.client.ui.view.scrollBoxView
import de.robolab.common.parser.isLineValid
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import tornadofx.*
import java.util.*
import java.util.regex.Pattern

/**
 * @author lars
 */
@Suppress("RedundantLambdaArrow")
class InfoBarFileEditView(private val content: InfoBarFileEdit) : View() {

    private val editor: CodeArea = CodeArea()
    private val scrollPane = VirtualizedScrollPane(editor)

    private fun updateContent(box: VBox) {
        box.clear()

        when (val content = content.detailBoxProperty.value) {
            is PlanetStatisticsDetailBox -> {
                box.add(DetailBoxPlanetStatistics(content))
            }
            is PathDetailBox -> {
                box.add(DetailBoxPath(content))
            }
            is PointDetailBox -> {
                box.add(DetailBoxPoint(content))
            }
        }
    }

    private fun updateActionList(box: VBox) {
        box.clear()

        for (hint in content.actionHintList.value) {
            box.hbox {
                paddingTop = 4
                paddingBottom = 4
                paddingLeft = 8
                paddingRight = 8
                vbox {
                    alignment = Pos.CENTER
                    icon(
                        when (hint.action) {
                            is ActionHint.Action.KeyboardAction -> MaterialIcon.KEYBOARD
                            is ActionHint.Action.PointerAction -> MaterialIcon.MOUSE
                        }
                    ) {
                        opacity = 0.3
                    }
                }
                vbox {
                    paddingLeft = 8
                    label(hint.action.toString()) {
                        style {
                            fontSize = 0.8.em
                        }
                    }
                    label(hint.description)
                }
            }
        }
    }

    override val root = vbox {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        vbox {
            vgrow = Priority.ALWAYS
            scrollBoxView {
                resizeBox(0.5, true) {
                    anchorpane {
                        hgrow = Priority.ALWAYS
                        vgrow = Priority.ALWAYS

                        add(scrollPane)

                        scrollPane.anchorpaneConstraints {
                            bottomAnchor = 0.0
                            leftAnchor = 0.0
                            topAnchor = 0.0
                            rightAnchor = 0.0
                        }
                    }
                }
                scrollBox(0.3) {
                    this@InfoBarFileEditView.content.detailBoxProperty.onChange {
                        updateContent(this)
                    }
                    updateContent(this)
                }
                scrollBox(0.2) {
                    this@InfoBarFileEditView.content.actionHintList.onChange {
                        updateActionList(this)
                    }
                    updateActionList(this)
                }
            }
        }
    }

    private val contentProperty = content.contentProperty

    private var updateOnTextChange = true

    private fun change() {
        updateOnTextChange = false
        editor.replaceText(contentProperty.value)
        updateOnTextChange = true
    }

    init {
        editor.undoManager.close()
        editor.addClass(MainStyle.codeArea)

        editor.hgrow = Priority.ALWAYS
        editor.vgrow = Priority.ALWAYS

        contentProperty.onChange {
            change()
        }
        change()

        editor.richChanges()
            .filter { ch -> ch.inserted != ch.removed }
            .subscribe {
                if (updateOnTextChange) {
                    try {
                        contentProperty.value = editor.text
                    } catch (_: Exception) {
                    }
                }
                updateStyle()
            }
        editor.selectionProperty().addListener { _ ->
            if (updateOnTextChange) {
                content.selectLine(editor.currentParagraph)
            }
        }

        editor.setOnKeyPressed { event ->
            if (event.isControlDown && event.code == KeyCode.Z) {
                content.undo()
            }
            if (event.isControlDown && event.isShiftDown && event.code == KeyCode.Z) {
                content.redo()
            }
        }

        editor.minWidth = 250.0
        editor.minHeight = 300.0
        updateStyle()

        editor.isFocusTraversable = true

        content.onSetLine { line ->
            editor.moveTo(line, 0)
        }
    }

    private fun updateStyle() {
        val pos = editor.caretPosition
        editor.setStyleSpans(0, computeHighlighting(editor.text))
        editor.selectRange(pos, pos)
    }

    private fun computeHighlighting(text: String): StyleSpans<Collection<String>>? {
        val lines = text.split("\n")
        var splits = lines.size
        val spansBuilder = StyleSpansBuilder<Collection<String>>()
        for (line in lines) {
            if (isLineValid(line)) {
                val matcher = PATTERN.matcher(line)
                var lastKwEnd = 0
                while (matcher.find()) {
                    val styleClass = when {
                        matcher.group("KEYWORD") != null -> "editor-keyword"
                        matcher.group("DIRECTION") != null -> "editor-direction"
                        matcher.group("NUMBER") != null -> "editor-number"
                        matcher.group("STRING") != null -> "editor-string"
                        matcher.group("HASH") != null -> "editor-comment"
                        matcher.group("COMMENT") != null -> "editor-comment"
                        matcher.group("TRAILING") != null -> "editor-error"
                        else -> "editor-default"
                    }
                    spansBuilder.add(Collections.singleton("editor-default"), matcher.start() - lastKwEnd)
                    spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start())
                    lastKwEnd = matcher.end()
                }
                spansBuilder.add(Collections.singleton("editor-default"), line.length - lastKwEnd)
            } else {
                if (line.trimStart().startsWith("#")) {
                    spansBuilder.add(Collections.singleton("editor-comment"), line.length)
                } else {
                    spansBuilder.add(Collections.singleton("editor-error"), line.length)
                }
            }

            splits -= 1
            if (splits > 0) {
                spansBuilder.add(Collections.singleton("editor-default"), 1)
            }
        }

        return spansBuilder.create()
    }

    companion object {
        private val KEYWORDS = arrayOf(
            "comment",
            "left",
            "center",
            "right",
            "name",
            "version",
            "spline",
            "grouping",
            "comment",
            "blue",
            "start",
            "target",
            "direction"
        )

        private val KEYWORD_PATTERN = ("\\b(" + KEYWORDS.joinToString("|") + ")\\b").toRegex()
        private val DIRECTION_PATTERN = "\\b([NESWnesw])\\b".toRegex()
        private val NUMBER_PATTERN = "(-?[0-9]+)".toRegex()
        private val STRING_PATTERN = "[a-zA-Z]".toRegex()
        private val HASH_PATTERN = "^#".toRegex()
        private val COMMENT_PATTERN = "#.*".toRegex()
        private val TRAILING_WHITESPACE = "\\s*$".toRegex()

        private val PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<DIRECTION>" + DIRECTION_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<HASH>" + HASH_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<TRAILING>" + TRAILING_WHITESPACE + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
        )
    }
}
