package de.robolab.jfx.view

import de.robolab.app.controller.InfoBarController
import de.robolab.app.controller.TraverserBarController
import de.robolab.app.model.file.InfoBarFileEditor
import de.robolab.app.model.file.InfoBarTraverser
import de.robolab.app.model.group.InfoBarGroupInfo
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import de.robolab.jfx.utils.buttonGroup
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.mapBinding
import javafx.scene.control.ToolBar
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class InfoBar(private val infoBarController: InfoBarController) : View() {

    private fun updateHeader(header: ToolBar) {
        header.items.clear()

        val list = infoBarController.contentListProperty.value
        if (list.size <= 1) return

        header.spacer()
        header.buttonGroup {
            for (btn in list) {
                button(btn.nameProperty.toFx()) {
                    bindSelectedProperty(infoBarController.selectedContentProperty.mapBinding { it == btn }) {
                        infoBarController.selectContent(btn)
                    }
                }
            }
        }
        header.spacer()
    }

    private fun updateContent(contentBox: VBox) {
        contentBox.clear()

        val content = infoBarController.selectedContentProperty.value ?: return

        when (content) {
            is InfoBarFileEditor -> {
                contentBox.add(InfoBarPlanetEditorView(content.contentProperty))
            }
            is InfoBarTraverser -> {
                if (content.traverserProperty.value == null) {
                    content.traverse()
                }
                contentBox.add(NullableViewContainer(content.traverserProperty))
            }
            is InfoBarGroupInfo -> {
                contentBox.add(InfoBarGroupView(content))
            }
        }
    }

    override val root = vbox {
        addClass(MainStyle.infoBar)
        vgrow = Priority.ALWAYS
        minWidth = 200.0

        toolbar {
            infoBarController.contentListProperty.onChange {
                updateHeader(this)
            }
            updateHeader(this)

            visibleWhen {
                infoBarController.contentListProperty.mapBinding { it.size > 1 }.toFx()
            }

            style {
                padding = box(0.5.em, 0.5.em)
            }
        }

        vbox {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            infoBarController.selectedContentProperty.onChange {
                updateContent(this)
            }
            updateContent(this)
        }
    }
}

class NullableViewContainer(private val traverserProperty: ReadOnlyProperty<TraverserBarController?>) : View() {

    private var prop: Property<TraverserBarController>? = null
    private var view: InfoBarTraverserView? = null

    private fun updateView() {
        val traverser = traverserProperty.value

        if (traverser == null) {
            root.clear()
            return
        }

        if (prop == null) {
            prop = de.westermann.kobserve.property.property(traverser)
        } else {
            prop?.value = traverser
        }
        if (view == null) {
            view = InfoBarTraverserView(prop!!)
        }

        root.add(view!!)
    }

    override val root = vbox { }

    init {
        traverserProperty.onChange {
            updateView()
        }
        updateView()
    }
}
