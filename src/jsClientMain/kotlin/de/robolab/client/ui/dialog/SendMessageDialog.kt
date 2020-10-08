package de.robolab.client.ui.dialog

import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.ui.views.utils.buttonGroup
import de.westermann.kobserve.event.emit
import de.westermann.kwebview.components.*

class SendMessageDialog private constructor(
    private val controller: SendMessageController
) :
    Dialog("Send message") {

    init {
        tab {
            classList += "dialog-form"
            dialogFormEntry("Topic") {
                boxView {
                    style {
                        width = "100%"
                    }
                    buttonGroup {
                        button("Explorer") {
                            onClick {
                                controller.topicExplorer()
                            }
                        }
                        button("Planet") {
                            onClick {
                                controller.topicPlanet()
                            }
                        }
                        button("Controller") {
                            onClick {
                                controller.topicController()
                            }
                        }
                    }
                    boxView("dialog-form-flex") {
                        inputView(controller.topicProperty)
                    }
                }
            }
            dialogFormEntry("From") {
                selectView(controller.fromProperty)
            }
            dialogFormEntry("Type") {
                buttonGroup {
                    classList += "dialog-form-flex"
                    style {
                        width = "100%"
                    }
                    selectView(controller.typeProperty)
                    button("Topic") {
                        onClick {
                            controller.topicByType()
                        }
                    }
                }
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
        fun open(controller: SendMessageController) {
            open(SendMessageDialog(controller))
        }
    }
}
