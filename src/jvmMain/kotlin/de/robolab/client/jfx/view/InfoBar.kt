package de.robolab.client.jfx.view

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.model.file.InfoBarFileEditor
import de.robolab.client.app.model.file.InfoBarTraverser
import de.robolab.client.app.model.file.PlanetStatisticsDetailBox
import de.robolab.client.app.model.group.InfoBarGroupInfo
import de.robolab.client.app.model.group.JsonDetailBox
import de.robolab.client.jfx.adapter.toFx
import de.robolab.client.jfx.style.MainStyle
import de.robolab.client.jfx.utils.buttonGroup
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import javafx.scene.Cursor
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import tornadofx.*
import kotlin.math.max
import kotlin.math.min

class InfoBar(private val infoBarController: InfoBarController) : View() {

    private var detailBoxSize = 0.3

    private fun updateHeader(header: HBox) {
        header.clear()

        val list = infoBarController.contentListProperty.value
        if (list.size <= 1) {
            header.minHeight = 0.0
            header.prefHeight = 0.0
            header.maxHeight = 0.0

            return
        }
        header.minHeight = Region.USE_COMPUTED_SIZE
        header.prefHeight = Region.USE_COMPUTED_SIZE
        header.maxHeight = Region.USE_COMPUTED_SIZE

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
                contentBox.add(InfoBarPlanetEditorView(content))
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

    private fun updateDetailBox(detailBox: VBox) {
        detailBox.clear()

        val content = infoBarController.detailBoxProperty.value ?: return

        when (content) {
            is JsonDetailBox -> {
                detailBox.add(DetailBoxJson(content))
            }
            is PlanetStatisticsDetailBox -> {
                detailBox.add(DetailBoxPlanetStatistics(content))
            }
        }
    }

    override val root = vbox {
        addClass(MainStyle.infoBar)
        vgrow = Priority.ALWAYS
        minWidth = 200.0

        val rootView = this

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

        val contentBoxView = vbox {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            infoBarController.selectedContentProperty.onChange {
                updateContent(this)
            }
            updateContent(this)
        }

        val detailBoxView = anchorpane {
            addClass(MainStyle.detailBox)
            hgrow = Priority.ALWAYS
            val detailBoxView = this

            scrollpane {
                anchorpaneConstraints {
                    topAnchor = 0.0
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }

                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                vbox {
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS

                    infoBarController.detailBoxProperty.onChange {
                        updateDetailBox(this)
                    }
                    updateDetailBox(this)
                }
            }

            hbox {
                anchorpaneConstraints {
                    topAnchor = 0.0
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                }

                minHeight = 10.0
                prefHeight = 10.0
                maxHeight = 10.0

                style {
                    cursor = Cursor.N_RESIZE
                }

                setOnMouseDragged { event ->
                    if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
                        val bounds = rootView.localToScreen(0.0, 0.0)
                        val height = event.screenY - bounds.y
                        val percent = 1.0 - (height / rootView.height)
                        detailBoxSize = max(0.05, min(0.95, percent))

                        val detailBoxHeight = rootView.height * detailBoxSize
                        contentBoxView.prefHeight = rootView.height - detailBoxHeight
                        detailBoxView.prefHeight = detailBoxHeight
                    }
                }
            }
        }

        rootView.heightProperty().onChange {
            val detailBoxHeight = rootView.height * detailBoxSize
            contentBoxView.prefHeight = rootView.height - detailBoxHeight
            detailBoxView.prefHeight = detailBoxHeight
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
