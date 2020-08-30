package de.robolab.client.ui.dialog

import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.communication.PathStatus
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.common.planet.Direction
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.flatMapMutableBinding
import de.westermann.kobserve.property.property
import javafx.scene.layout.Priority
import tornadofx.*

class SendMessageDialog : GenericDialog() {

    private val controllerParam: SendMessageController by param()

    private val controllerProperty = property(
        SendMessageController("", "") { _, _ -> false }
    )
    private val controller by controllerProperty

    override val root = buildContent("Send message") {
        form {
            fieldset {
                field("Topic") {
                    vbox {
                        buttonGroup {
                            hgrow = Priority.ALWAYS

                            button("Explorer") {
                                setOnAction {
                                    controller.topicExplorer()
                                }

                                hgrow = Priority.ALWAYS
                            }
                            button("Planet") {
                                setOnAction {
                                    controller.topicPlanet()
                                }

                                hgrow = Priority.ALWAYS
                            }
                            button("Controller") {
                                setOnAction {
                                    controller.topicController()
                                }

                                hgrow = Priority.ALWAYS
                            }
                        }
                        textfield(controllerProperty.flatMapMutableBinding { it.topicProperty }.toFx())
                    }
                }
                field("From") {
                    combobox(
                        controllerProperty.flatMapMutableBinding { it.fromProperty }.toFx(),
                        SendMessageController.From.values().toList()
                    )
                }
                field("Type") {
                    buttonGroup {
                        combobox(
                            controllerProperty.flatMapMutableBinding { it.typeProperty }.toFx(),
                            SendMessageController.Type.values().toList().sortedBy { it.displayName })
                        button("Topic") {
                            setOnAction {
                                controller.topicByType()
                            }
                        }
                    }
                }
            }
            fieldset {
                field("Planet name") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.planetNameProperty }.toFx(),
                        NullableStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.planetNameVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.planetNameVisibleProperty }.toFx())
                }
                field("Start X") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.startXProperty }.toFx(),
                        NullableIntStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.startXVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.startXVisibleProperty }.toFx())
                }
                field("Start Y") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.startYProperty }.toFx(),
                        NullableIntStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.startYVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.startYVisibleProperty }.toFx())
                }
                field("Start direction") {
                    combobox(
                        controllerProperty.flatMapMutableBinding { it.startDirectionProperty }.toFx(),
                        Direction.values().toList()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.startDirectionVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.startDirectionVisibleProperty }.toFx())
                }
                field("Start orientation") {
                    combobox(
                        controllerProperty.flatMapMutableBinding { it.startOrientationProperty }.toFx(),
                        Direction.values().toList()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.startOrientationVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.startOrientationVisibleProperty }.toFx())
                }
                field("End X") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.endXProperty }.toFx(),
                        NullableIntStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.endXVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.endXVisibleProperty }.toFx())
                }
                field("End Y") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.endYProperty }.toFx(),
                        NullableIntStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.endYVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.endYVisibleProperty }.toFx())
                }
                field("End direction") {
                    combobox(
                        controllerProperty.flatMapMutableBinding { it.endDirectionProperty }.toFx(),
                        Direction.values().toList()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.endDirectionVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.endDirectionVisibleProperty }.toFx())
                }
                field("Target X") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.targetXProperty }.toFx(),
                        NullableIntStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.targetXVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.targetXVisibleProperty }.toFx())
                }
                field("Target Y") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.targetYProperty }.toFx(),
                        NullableIntStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.targetYVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.targetYVisibleProperty }.toFx())
                }
                field("Path status") {
                    combobox(
                        controllerProperty.flatMapMutableBinding { it.pathStatusProperty }.toFx(),
                        PathStatus.values().toList()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.pathStatusVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.pathStatusVisibleProperty }.toFx())
                }
                field("Path weight") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.pathWeightProperty }.toFx(),
                        NullableIntStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.pathWeightVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.pathWeightVisibleProperty }.toFx())
                }
                field("Message") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.messageProperty }.toFx(),
                        NullableStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.messageVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.messageVisibleProperty }.toFx())
                }
                field("Custom json message") {
                    textfield(
                        controllerProperty.flatMapMutableBinding { it.customProperty }.toFx(),
                        NullableStringConverter()
                    )
                    visibleWhen(controllerProperty.flatMapBinding { it.customVisibleProperty }.toFx())
                    managedWhen(controllerProperty.flatMapBinding { it.customVisibleProperty }.toFx())
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

    override fun onBeforeShow() {
        super.onBeforeShow()

        controllerProperty.value = controllerParam
    }

    companion object {
        fun open(controller: SendMessageController) {
            open<SendMessageDialog>(
                "controllerParam" to controller,
            )
        }
    }
}
