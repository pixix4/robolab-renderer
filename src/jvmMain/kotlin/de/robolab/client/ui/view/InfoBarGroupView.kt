package de.robolab.client.ui.view

import com.soywiz.klock.format
import de.robolab.client.app.model.group.InfoBarGroupInfo
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.utils.PreferenceStorage
import javafx.scene.layout.Priority
import tornadofx.*

class InfoBarGroupView(private val contentInfo: InfoBarGroupInfo) : View() {

    override val root = vbox {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        form {
            fieldset("Attempt info") {
                field("Messages") {
                    label(contentInfo.messageCountStringProperty.toFx())
                }
                field("First message") {
                    label(contentInfo.firstMessageTimeStringProperty.toFx())
                }
                field("Last message") {
                    label(contentInfo.lastMessageTimeStringProperty.toFx())
                }
                field("Attempt duration") {
                    label(contentInfo.attemptDurationStringProperty.toFx())
                }
            }

            fieldset("Send messages") {
                button("Open dialog") {
                    setOnAction {
                        contentInfo.openSendDialog()
                    }
                }
            }

            fieldset("Exam") {
                visibleWhen(PreferenceStorage.examActiveProperty.toFx())
                managedWhen(PreferenceStorage.examActiveProperty.toFx())

                buttonGroup {
                    button("Small Planet") {
                        setOnAction {
                            contentInfo.openSendDialogSmallExamPlanet()
                        }
                    }
                    button("Large Planet") {
                        setOnAction {
                            contentInfo.openSendDialogLargeExamPlanet()
                        }
                    }
                }
            }
        }

        tableview(contentInfo.messages.toFx()) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            column<RobolabMessage, String>("Time") {
                InfoBarGroupInfo.TIME_FORMAT_DETAILED.format(it.value.metadata.time).toProperty()
            }
            column<RobolabMessage, String>("From") {
                it.value.metadata.from.name.toLowerCase().capitalize().toProperty()
            }
            column<RobolabMessage, String>("Summary") {
                it.value.summary.toProperty()
            }

            onSelectionChange {
                val index = contentInfo.messages.indexOf(it ?: return@onSelectionChange)
                contentInfo.selectedIndexProperty.value = index
            }
            selectionModel.selectedIndexProperty().onChange {
                if (it != contentInfo.selectedIndexProperty.value)
                    contentInfo.selectedIndexProperty.value = it
            }

            contentInfo.selectedIndexProperty.onChange {
                if (contentInfo.selectedIndexProperty.value != selectionModel.selectedIndex) {
                    selectionModel.select(contentInfo.selectedIndexProperty.value)
                }
            }
        }
    }
}
