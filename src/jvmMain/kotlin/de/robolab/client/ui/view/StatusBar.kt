package de.robolab.client.ui.view

import de.robolab.client.app.controller.StatusBarController
import de.robolab.client.app.controller.UiController
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.updater.Downloader
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.parser.toFixed
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import tornadofx.*

class StatusBar(
    private val statusBarController: StatusBarController,
    private val uiController: UiController,
) : View(), Downloader.ProgressListener {

    private val progressProperty = property<Double>()

    private fun updateStatusBar(box: HBox) {
        box.clear()

        for (element in statusBarController.entryListProperty.value) {
            box.label(element)
        }
    }

    override val root = hbox {
        addClass(MainStyle.statusBar)

        hbox {
            addClass(MainStyle.statusBarStatus)
            bindClass(
                MainStyle.success,
                statusBarController.statusColor.mapBinding { it == StatusBarController.StatusColor.SUCCESS })
            bindClass(
                MainStyle.warn,
                statusBarController.statusColor.mapBinding { it == StatusBarController.StatusColor.WARN })
            bindClass(
                MainStyle.error,
                statusBarController.statusColor.mapBinding { it == StatusBarController.StatusColor.ERROR })

            style {
                prefHeight = 2.em
                padding = box(0.4.em, 0.5.em)
            }

            label(statusBarController.statusMessage.toFx())
            spacer()
            label(statusBarController.statusActionLabel.toFx()) {
                setOnMouseClicked {
                    statusBarController.onStatusAction()
                }

                style {
                    underline = true
                }
            }

            uiController.navigationBarWidthProperty.onChange {
                val width = uiController.navigationBarWidthProperty.value
                minWidth = width
                maxWidth = width
                prefWidth = width
            }
        }

        hbox {
            addClass(MainStyle.statusBarBoxes)
            statusBarController.entryListProperty.onChange {
                updateStatusBar(this)
            }
            updateStatusBar(this)
        }

        spacer()

        hbox {
            val visible = progressProperty.mapBinding { it != null }.toFx()
            val progress = progressProperty.mapBinding { it ?: 0.0 }
            val progressLabel = progress.mapBinding { (it * 100.0).toFixed(2) + "%" }

            visibleWhen(visible)
            managedWhen(visible)

            alignment = Pos.CENTER

            progressbar(progress.toFx())
            label(progressLabel.toFx())
        }

        hbox {
            addClass(MainStyle.memoryIndicator)

            val memoryProperty = SimpleDoubleProperty(0.0)
            val memoryLabelProperty = SimpleStringProperty("0 of 0M")

            anchorpane {
                progressbar(memoryProperty) {
                    anchorpaneConstraints {
                        topAnchor = 0
                        bottomAnchor = 0
                        leftAnchor = 0
                        rightAnchor = 0
                    }
                }
                label(memoryLabelProperty) {
                    paddingLeft = 4
                    paddingRight = 4
                    alignment = Pos.CENTER
                    anchorpaneConstraints {
                        topAnchor = 0
                        bottomAnchor = 0
                        leftAnchor = 0
                        rightAnchor = 0
                    }
                }
            }

            runAfterTimeoutInterval(1000) {
                val totalMemory = Runtime.getRuntime().totalMemory()
                val freeMemory = Runtime.getRuntime().freeMemory()
                memoryProperty.value = (totalMemory - freeMemory).toDouble() / totalMemory.toDouble()

                val totalStr = "${totalMemory / 1024 / 1024}"
                val usedStr = "${(totalMemory - freeMemory) / 1024 / 1024}"
                memoryLabelProperty.value = "$usedStr of ${totalStr}M"
            }
        }
    }

    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        if (done) {
            progressProperty.value = null
        } else {
            progressProperty.value = bytesRead.toDouble() / contentLength.toDouble()
        }
    }
}
