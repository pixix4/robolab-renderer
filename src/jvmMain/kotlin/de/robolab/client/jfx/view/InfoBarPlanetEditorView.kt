package de.robolab.client.jfx.view

import de.robolab.client.app.model.file.InfoBarFileEditor
import de.robolab.client.jfx.style.MainStyle
import de.robolab.common.parser.FileLine
import de.westermann.kobserve.base.ObservableProperty
import javafx.scene.Parent
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
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
class InfoBarPlanetEditorView(private val content: InfoBarFileEditor) : View() {
    private val editor: CodeArea = CodeArea()
    private val scrollPane = VirtualizedScrollPane(editor)
    override val root = AnchorPane().apply {
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
                // TODO Cursor
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
            val valid =
                FileLine.PathLine.REGEX.matches(line) ||
                        FileLine.StartPointLine.REGEX.matches(line) ||
                        FileLine.TargetLine.REGEX.matches(line) ||
                        FileLine.BluePointLine.REGEX.matches(line) ||
                        FileLine.PathSelectLine.REGEX.matches(line) ||
                        FileLine.NameLine.REGEX.matches(line) ||
                        FileLine.SplineLine.REGEX.matches(line) ||
                        FileLine.CommentLine.REGEX.matches(line) ||
                        FileLine.CommentSubLine.REGEX.matches(line) ||
                        FileLine.HiddenLine.REGEX.matches(line)

            if (valid) {
                val matcher = PATTERN.matcher(line)
                var lastKwEnd = 0
                while (matcher.find()) {
                    val styleClass = (when {
                        matcher.group("KEYWORD") != null -> "editor-keyword"
                        matcher.group("DIRECTION") != null -> "editor-direction"
                        matcher.group("NUMBER") != null -> "editor-number"
                        matcher.group("STRING") != null -> "editor-string"
                        matcher.group("HASH") != null -> "editor-comment"
                        matcher.group("COMMENT") != null -> "editor-error"
                        matcher.group("TRAILING") != null -> "editor-error"
                        else -> ""
                    })
                    spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd)
                    spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start())
                    lastKwEnd = matcher.end()
                }
                spansBuilder.add(Collections.emptyList(), line.length - lastKwEnd)
            } else {
                if (line.trimStart().startsWith("#")) {
                    spansBuilder.add(Collections.singleton("comment"), line.length)
                } else {
                    spansBuilder.add(Collections.singleton("error"), line.length)
                }
            }

            splits -= 1
            if (splits > 0) {
                spansBuilder.add(Collections.emptyList(), 1)
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
            "spline",
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
