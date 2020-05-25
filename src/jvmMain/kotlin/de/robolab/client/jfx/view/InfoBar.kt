package de.robolab.client.jfx.view

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.model.file.InfoBarFileEditor
import de.robolab.client.app.model.file.InfoBarTraverser
import de.robolab.client.app.model.group.InfoBarGroupInfo
import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.style.MainStyle
import de.robolab.client.jfx.utils.buttonGroup
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class InfoBar(private val infoBarController: InfoBarController) : View() {

    private fun updateHeader(header: HBox) {
        header.clear()

        val list = infoBarController.contentListProperty.value
        if (list.size <= 1) return

        header.buttonGroup {
            hgrow = Priority.ALWAYS

            for (btn in list) {
                button(btn.nameProperty.toFx()) {
                    bindSelectedProperty(infoBarController.selectedContentProperty.mapBinding { it == btn }) {
                        infoBarController.selectContent(btn)
                    }

                    hgrow = Priority.ALWAYS
                }
            }
        }
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

        hbox {
            addClass(MainStyle.toolBar)
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

class NullableViewContainer(private val traverserProperty: ObservableValue<TraverserBarController?>) : View() {

    private var prop: ObservableProperty<TraverserBarController>? = null
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