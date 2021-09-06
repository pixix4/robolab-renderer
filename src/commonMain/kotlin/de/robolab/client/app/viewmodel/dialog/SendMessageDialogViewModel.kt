package de.robolab.client.app.viewmodel.dialog

import de.robolab.client.app.controller.dialog.SendMessageDialogController
import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.app.viewmodel.FormViewModel
import de.robolab.client.app.viewmodel.build
import de.robolab.client.app.viewmodel.buildForm
import de.westermann.kobserve.event.now

class SendMessageDialogViewModel(
    private val controller: SendMessageDialogController
) : DialogViewModel("Send message") {

    private lateinit var contentGroup: FormViewModel.Group

    val content = buildForm {
        group {
            labeledEntry("Topic") {
                button("Explorer") {
                    controller.topicExplorer()
                }
                button("Planet") {
                    controller.topicPlanet()
                }
                button("Controller group") {
                    controller.topicControllerGroup()
                }
                button("Controller admin") {
                    controller.topicControllerAdmin()
                }
            }
            entry {
                input(controller.topicProperty)
            }
            labeledEntry("From") {
                select(controller.fromProperty) { from ->
                    from.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
            labeledEntry("Type") {
                select(controller.typeProperty)
                button("Topic") {
                    controller.topicByType()
                }
            }
        }

        contentGroup = group {}

        group {
            entry {
                button("Send") {
                    if (controller.send()) {
                        close()
                    }
                }
            }
        }
    }

    private fun updateContentGroup() {
        contentGroup.build {
            clear()

            if (controller.planetNameVisibleProperty.value) {
                labeledEntry("Planet name") {
                    input(controller.planetNameProperty)
                }
            }
            if (controller.startXVisibleProperty.value) {
                labeledEntry("Start X") {
                    input(controller.startXProperty, coordinateRange)
                }
            }
            if (controller.startYVisibleProperty.value) {
                labeledEntry("Start Y") {
                    input(controller.startYProperty, coordinateRange)
                }
            }
            if (controller.startDirectionVisibleProperty.value) {
                labeledEntry("Start direction") {
                    select(controller.startDirectionProperty)
                }
            }
            if (controller.startOrientationVisibleProperty.value) {
                labeledEntry("Start orientation") {
                    select(controller.startOrientationProperty)
                }
            }
            if (controller.endXVisibleProperty.value) {
                labeledEntry("End X") {
                    input(controller.endXProperty, coordinateRange)
                }
            }
            if (controller.endYVisibleProperty.value) {
                labeledEntry("End Y") {
                    input(controller.endYProperty, coordinateRange)
                }
            }
            if (controller.endDirectionVisibleProperty.value) {
                labeledEntry("End direction") {
                    select(controller.endDirectionProperty)
                }
            }
            if (controller.targetXVisibleProperty.value) {
                labeledEntry("Target X") {
                    input(controller.targetXProperty, coordinateRange)
                }
            }
            if (controller.targetYVisibleProperty.value) {
                labeledEntry("Target Y") {
                    input(controller.targetYProperty, coordinateRange)
                }
            }
            if (controller.pathStatusVisibleProperty.value) {
                labeledEntry("Path status") {
                    select(controller.pathStatusProperty)
                }
            }
            if (controller.pathWeightVisibleProperty.value) {
                labeledEntry("Path weight") {
                    input(controller.pathWeightProperty, coordinateRange)
                }
            }
            if (controller.messageVisibleProperty.value) {
                labeledEntry("Message") {
                    input(controller.messageProperty)
                }
            }
            if (controller.groupIdVisibleProperty.value) {
                labeledEntry("Group ID") {
                    input(controller.groupIdProperty)
                }
            }
            if (controller.customVisibleProperty.value) {
                labeledEntry("Custom json message") {
                    input(controller.customProperty)
                }
            }
        }
    }

    init {
        controller.typeProperty.onChange.now {
            updateContentGroup()
        }
    }

    companion object {
        private val coordinateRange = LongRange((-10e10).toLong(), 10e10.toLong())
    }
}
