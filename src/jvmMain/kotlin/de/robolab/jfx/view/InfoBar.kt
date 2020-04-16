package de.robolab.jfx.view

import de.robolab.app.controller.InfoBarController
import de.robolab.app.model.file.InfoBarFileEditor
import de.robolab.app.model.file.InfoBarTraverser
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import de.robolab.jfx.utils.buttonGroup
import de.westermann.kobserve.ReadOnlyProperty
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
            for (btn in list) {
                togglebutton(btn.nameProperty.toFx()) {
                    bindSelectedProperty(infoBarController.selectedContentProperty.mapBinding { it == btn }) {
                        infoBarController.selectContent(btn)
                    }
                }
            }
        }
    }

    private fun updateContent(contentBox: VBox) {
        contentBox.clear()

        val content = infoBarController.selectedContentProperty.value ?: return

        when (content) {
            is InfoBarFileEditor -> {
                contentBox.add(PlanetTextEditor(content.contentProperty))
            }
            is InfoBarTraverser -> {
                contentBox.button("Traverse") {
                    setOnAction {
                        content.traverse()
                    }
                }
            }
        }
    }

    override val root = vbox {
        addClass(MainStyle.infoBar)
        vgrow = Priority.ALWAYS
        minWidth = 200.0

        hbox {
            infoBarController.contentListProperty.onChange {
                updateHeader(this)
            }
            updateHeader(this)

            style {
                padding = box(0.2.em, 0.5.em)
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
