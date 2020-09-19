package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.InfoBarFileEdit
import de.robolab.client.app.model.file.PathDetailBox
import de.robolab.client.app.model.file.PlanetStatisticsDetailBox
import de.robolab.client.app.model.file.PointDetailBox
import de.robolab.client.renderer.view.base.ActionHint
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.iconView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.scrollBoxView
import org.w3c.dom.HTMLElement

class InfoBarFileEditView(
    private val content: InfoBarFileEdit,
    private val uiController: UiController
) : ViewCollection<View>() {

    private fun updateContent(box: BoxView) {
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

    private fun updateActionList(box: BoxView) {
        box.clear()

        for (hint in content.actionHintList.value) {
            box.boxView {
                style {
                    padding = "0.3rem 0.4rem"
                    whiteSpace = "nowrap"
                }

                boxView {
                    style {
                        display = "inline-block"
                    }
                    iconView(when (hint.action) {
                        is ActionHint.Action.KeyboardAction -> MaterialIcon.KEYBOARD
                        is ActionHint.Action.PointerAction -> MaterialIcon.MOUSE
                    })
                }
                boxView {
                    style {
                        display = "inline-block"
                        lineHeight = "1rem"
                        paddingLeft = "0.4rem"
                    }
                    textView(hint.action.toString()) {
                        style {
                            display = "block"
                            fontSize = "0.8rem"
                        }
                    }
                    textView(hint.description) {
                        style {
                            display = "block"
                        }
                    }
                }
            }
        }
    }


    init {
        scrollBoxView {
            uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(0.5) {
                val editorContainer = boxView("text-editor-container")

                val editor = TextEditor(editorContainer.html)

                var ignoreUpdate = true
                editor.value = content.content
                content.contentProperty.onChange {
                    ignoreUpdate = true
                    editor.value = content.content
                    ignoreUpdate = false
                }

                editor.addOnChangeListener {
                    ignoreUpdate = true
                    content.contentProperty.value = editor.value
                    ignoreUpdate = false
                }
                editor.addOnCursorListener { line, _ ->
                    if (!ignoreUpdate) {
                        content.selectLine(line)
                    }
                }

                uiController.infoBarVisibleProperty.onChange {
                    editor.refresh()
                }
                uiController.infoBarWidthProperty.onChange {
                    editor.refresh()
                }
                onResize {
                    editor.refresh()
                }

                content.onSetLine { line ->
                    if (!ignoreUpdate) {
                        editor.setCursor(line, 0)
                    }
                }
                ignoreUpdate = false
            }
            resizeBox(0.3) {
                content.detailBoxProperty.onChange {
                    updateContent(this)
                }
                updateContent(this)
            }
            resizeBox(0.2) {
                content.actionHintList.onChange {
                    updateActionList(this)
                }
                updateActionList(this)
            }
        }
    }
}

external class TextEditor(container: HTMLElement) {
    var value: String

    fun addOnChangeListener(callback: () -> Unit)
    fun addOnCursorListener(callback: (line: Int, ch: Int) -> Unit)

    fun setCursor(line: Int, ch: Int)

    fun refresh()
}
