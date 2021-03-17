package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.details.InfoBarFileEdit
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.renderer.view.base.ActionHint
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.bindView
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.scrollBoxView
import org.w3c.dom.HTMLElement

class InfoBarFileEditView(
    private val viewModel: InfoBarFileEdit,
) : ViewCollection<View>() {

    private fun updateActionList(box: BoxView) {
        box.clear()

        for (hint in viewModel.actionHintList.value) {
            box.boxView {
                style {
                    padding = "0.3rem 0.4rem"
                    whiteSpace = "nowrap"
                }

                boxView {
                    style {
                        display = "inline-block"
                    }
                    iconView(
                        when (hint.action) {
                            is ActionHint.Action.KeyboardAction -> MaterialIcon.KEYBOARD
                            is ActionHint.Action.PointerAction -> MaterialIcon.MOUSE
                        }
                    )
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
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(0.5) {
                classList += "text-editor-box"

                val editorContainer = boxView("text-editor-container")

                val editor = TextEditor(editorContainer.html)

                var ignoreUpdate = true
                editor.value = viewModel.content
                viewModel.stringContentProperty.onChange {
                    ignoreUpdate = true
                    editor.value = viewModel.content
                    ignoreUpdate = false
                }

                editor.addOnChangeListener {
                    ignoreUpdate = true
                    viewModel.stringContentProperty.value = editor.value
                    ignoreUpdate = false
                }
                editor.addOnCursorListener { line, _ ->
                    if (!ignoreUpdate) {
                        viewModel.selectLine(line)
                    }
                }

                viewModel.uiController.infoBarVisibleProperty.onChange {
                    editor.refresh()
                }
                viewModel.uiController.infoBarWidthProperty.onChange {
                    editor.refresh()
                }
                onResize {
                    editor.refresh()
                }

                viewModel.onSetLine { line ->
                    if (!ignoreUpdate) {
                        editor.setCursor(line, 0)
                    }
                }
                ignoreUpdate = false
            }
            resizeBox(0.3) {
                bindView(viewModel.detailBoxProperty) {
                    ViewFactoryRegistry.create(it)
                }
            }
            resizeBox(0.2) {
                viewModel.actionHintList.onChange {
                    updateActionList(this)
                }
                updateActionList(this)
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarFileEdit
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarFileEditView(viewModel as InfoBarFileEdit)
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
