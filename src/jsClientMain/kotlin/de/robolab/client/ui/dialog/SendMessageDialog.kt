package de.robolab.client.ui.dialog

import de.robolab.client.app.controller.SendMessageController
import de.westermann.kobserve.event.emit
import de.westermann.kwebview.components.*

class SendMessageDialog private constructor(topic: String, sendMessage: (String, String) -> Boolean) :
    Dialog("Send message") {

    private val controller = SendMessageController(topic, sendMessage)

    init {
        tab {
            dialogFormEntry("Topic") {
                inputView(controller.topicProperty)
            }
            dialogFormEntry("From") {
                selectView(controller.fromProperty)
            }
            dialogFormEntry("Type") {
                selectView(controller.typeProperty)
            }

            boxView("dialog-hide-entry") {
                dialogFormEntry("Planet name") {
                    inputView(controller.planetNameProperty.bindStringParsing())
                }.classList.bind("active", controller.planetNameVisibleProperty)
                dialogFormEntry("Start X") {
                    inputView(controller.startXProperty.bindStringParsing())
                }.classList.bind("active", controller.startXVisibleProperty)
                dialogFormEntry("Start Y") {
                    inputView(controller.startYProperty.bindStringParsing())
                }.classList.bind("active", controller.startYVisibleProperty)
                dialogFormEntry("Start direction") {
                    selectViewNullable(controller.startDirectionProperty)
                }.classList.bind("active", controller.startDirectionVisibleProperty)
                dialogFormEntry("Start orientation") {
                    selectViewNullable(controller.startOrientationProperty)
                }.classList.bind("active", controller.startOrientationVisibleProperty)
                dialogFormEntry("End X") {
                    inputView(controller.endXProperty.bindStringParsing())
                }.classList.bind("active", controller.endXVisibleProperty)
                dialogFormEntry("End Y") {
                    inputView(controller.endYProperty.bindStringParsing())
                }.classList.bind("active", controller.endYVisibleProperty)
                dialogFormEntry("End direction") {
                    selectViewNullable(controller.endDirectionProperty)
                }.classList.bind("active", controller.endDirectionVisibleProperty)
                dialogFormEntry("Target X") {
                    inputView(controller.targetXProperty.bindStringParsing())
                }.classList.bind("active", controller.targetXVisibleProperty)
                dialogFormEntry("Target Y") {
                    inputView(controller.targetYProperty.bindStringParsing())
                }.classList.bind("active", controller.targetYVisibleProperty)
                dialogFormEntry("Path status") {
                    selectViewNullable(controller.pathStatusProperty)
                }.classList.bind("active", controller.pathStatusVisibleProperty)
                dialogFormEntry("Path weight") {
                    inputView(controller.pathWeightProperty.bindStringParsing())
                }.classList.bind("active", controller.pathWeightVisibleProperty)
                dialogFormEntry("Message") {
                    inputView(controller.messageProperty.bindStringParsing())
                }.classList.bind("active", controller.messageVisibleProperty)
                dialogFormEntry("Custom json message") {
                    inputView(controller.customProperty.bindStringParsing())
                }.classList.bind("active", controller.customVisibleProperty)
            }

            button("Send") {
                onClick {
                    if (controller.send()) {
                        this@SendMessageDialog.onClose.emit()
                    }
                }
            }
        }
    }

    companion object {
        fun open(topic: String, sendMessage: (String, String) -> Boolean) {
            open(SendMessageDialog(topic, sendMessage))
        }
    }
}
