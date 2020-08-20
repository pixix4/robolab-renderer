package de.robolab.client.ui.dialog

import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.communication.PathStatus
import de.robolab.client.ui.adapter.toFx
import de.robolab.common.planet.Direction
import tornadofx.*

class SendMessageDialog : GenericDialog() {

    private val topic: String by param()
    private val sendMessage: (String, String) -> Boolean by param()

    private val controller = SendMessageController(topic, sendMessage)

    override val root = buildContent("Send message") {
        form {
            fieldset {
                field("Topic") {
                    textfield(controller.topicProperty.toFx())
                }
                field("From") {
                    combobox(controller.fromProperty.toFx(), SendMessageController.From.values().toList())
                }
                field("Type") {
                    combobox(controller.typeProperty.toFx(), SendMessageController.Type.values().toList().sortedBy { it.displayName })
                }
            }
            fieldset {
                field("Planet name") {
                    textfield(controller.planetNameProperty.toFx(), NullableStringConverter())
                    visibleWhen(controller.planetNameVisibleProperty.toFx())
                    managedWhen(controller.planetNameVisibleProperty.toFx())
                }
                field("Start X") {
                    textfield(controller.startXProperty.toFx(), NullableIntStringConverter())
                    visibleWhen(controller.startXVisibleProperty.toFx())
                    managedWhen(controller.startXVisibleProperty.toFx())
                }
                field("Start Y") {
                    textfield(controller.startYProperty.toFx(), NullableIntStringConverter())
                    visibleWhen(controller.startYVisibleProperty.toFx())
                    managedWhen(controller.startYVisibleProperty.toFx())
                }
                field("Start direction") {
                    combobox(controller.startDirectionProperty.toFx(), Direction.values().toList())
                    visibleWhen(controller.startDirectionVisibleProperty.toFx())
                    managedWhen(controller.startDirectionVisibleProperty.toFx())
                }
                field("Start orientation") {
                    combobox(controller.startOrientationProperty.toFx(), Direction.values().toList())
                    visibleWhen(controller.startOrientationVisibleProperty.toFx())
                    managedWhen(controller.startOrientationVisibleProperty.toFx())
                }
                field("End X") {
                    textfield(controller.endXProperty.toFx(), NullableIntStringConverter())
                    visibleWhen(controller.endXVisibleProperty.toFx())
                    managedWhen(controller.endXVisibleProperty.toFx())
                }
                field("End Y") {
                    textfield(controller.endYProperty.toFx(), NullableIntStringConverter())
                    visibleWhen(controller.endYVisibleProperty.toFx())
                    managedWhen(controller.endYVisibleProperty.toFx())
                }
                field("End direction") {
                    combobox(controller.endDirectionProperty.toFx(), Direction.values().toList())
                    visibleWhen(controller.endDirectionVisibleProperty.toFx())
                    managedWhen(controller.endDirectionVisibleProperty.toFx())
                }
                field("Target X") {
                    textfield(controller.targetXProperty.toFx(), NullableIntStringConverter())
                    visibleWhen(controller.targetXVisibleProperty.toFx())
                    managedWhen(controller.targetXVisibleProperty.toFx())
                }
                field("Target Y") {
                    textfield(controller.targetYProperty.toFx(), NullableIntStringConverter())
                    visibleWhen(controller.targetYVisibleProperty.toFx())
                    managedWhen(controller.targetYVisibleProperty.toFx())
                }
                field("Path status") {
                    combobox(controller.pathStatusProperty.toFx(), PathStatus.values().toList())
                    visibleWhen(controller.pathStatusVisibleProperty.toFx())
                    managedWhen(controller.pathStatusVisibleProperty.toFx())
                }
                field("Path weight") {
                    textfield(controller.pathWeightProperty.toFx(), NullableIntStringConverter())
                    visibleWhen(controller.pathWeightVisibleProperty.toFx())
                    managedWhen(controller.pathWeightVisibleProperty.toFx())
                }
                field("Message") {
                    textfield(controller.messageProperty.toFx(), NullableStringConverter())
                    visibleWhen(controller.messageVisibleProperty.toFx())
                    managedWhen(controller.messageVisibleProperty.toFx())
                }
                field("Custom json message") {
                    textfield(controller.customProperty.toFx(), NullableStringConverter())
                    visibleWhen(controller.customVisibleProperty.toFx())
                    managedWhen(controller.customVisibleProperty.toFx())
                }
            }
            fieldset {
                button("Send") {
                    setOnAction {
                        if (controller.send()) {
                            close()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun open(topic: String, sendMessage: (String, String) -> Boolean) {
            open<SendMessageDialog>(
                "topic" to topic,
                "sendMessage" to sendMessage
            )
        }
    }
}
