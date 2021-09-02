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
import de.westermann.kwebview.sync
import org.w3c.dom.HTMLElement

class InfoBarFileEditView(
    private val viewModel: InfoBarFileEdit,
) : ViewCollection<View>() {

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

                ignoreUpdate = false
            }
            resizeBox(0.3) {
                bindView(viewModel.detailBoxProperty) {
                    ViewFactoryRegistry.create(it)
                }
            }
            resizeBox(0.2) {
                sync(
                    viewModel.actionHintList,
                    create = { item ->
                        ActionHintView(item)
                    },
                    update = { view, item ->
                        view.hint = item
                    },
                    delete = { view ->
                        view.hint = null
                    }
                )
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

class ActionHintView(hint: ActionHint): ViewCollection<View>() {

    var hint: ActionHint? = hint
        set(value) {
            field = value

            iconView.icon = when (hint?.action) {
                is ActionHint.Action.KeyboardAction -> MaterialIcon.KEYBOARD
                is ActionHint.Action.PointerAction -> MaterialIcon.MOUSE
                else -> null
            }
            actionTextView.text = hint?.action?.toString() ?: ""
            descriptionTextView.text = hint?.description ?: ""
        }

    lateinit var iconView: IconView
    lateinit var actionTextView: TextView
    lateinit var descriptionTextView: TextView

    init {
        style {
            padding = "0.3rem 0.4rem"
            whiteSpace = "nowrap"
        }

        boxView {
            style {
                display = "inline-block"
            }
            iconView = iconView()
        }
        boxView {
            style {
                display = "inline-block"
                lineHeight = "1rem"
                paddingLeft = "0.4rem"
            }
            actionTextView = textView {
                style {
                    display = "block"
                    fontSize = "0.8rem"
                }
            }
            descriptionTextView = textView {
                style {
                    display = "block"
                }
            }
        }

        this.hint = hint
    }

}
