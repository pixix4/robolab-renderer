package de.robolab.client.ui.view.boxes

import com.soywiz.klock.format
import de.robolab.client.app.model.group.InfoBarGroupMessages
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.ui.view.scrollBoxView
import de.robolab.client.utils.PreferenceStorage
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import tornadofx.*

class InfoBarGroupMessagesView(private val contentInfo: InfoBarGroupMessages) : View() {

    override val root = scrollBoxView {
        scrollBox(0.2) {
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
        }

        resizeBox(0.5, true) {
            tableview(contentInfo.messages.toFx()) {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                column<RobolabMessage, String>("Time") {
                    InfoBarGroupMessages.TIME_FORMAT_DETAILED.format(it.value.metadata.time).toProperty()
                }
                column<RobolabMessage, String>("From") {
                    it.value.metadata.from.name.toLowerCase().capitalize().toProperty()
                }
                column<RobolabMessage, String>("Summary") {
                    it.value.summary.toProperty()
                }.minWidth = 400.0

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
        scrollBox(0.3) {
            form {
                fieldset(contentInfo.headerProperty.value) {
                    contentInfo.headerProperty.onChange {
                        this.text = contentInfo.headerProperty.value
                    }
                    field("From") {
                        label(contentInfo.fromProperty.toFx())
                    }
                    field("Group") {
                        label(contentInfo.groupProperty.toFx())
                    }
                    field("Topic") {
                        label(contentInfo.topicProperty.toFx())
                    }
                    field("Time") {
                        label(contentInfo.timeProperty.toFx())
                    }
                }
                fieldset("Content") {
                    field("Details") {
                        label(contentInfo.detailsProperty.toFx())
                    }
                    field("Message") {
                        label(contentInfo.rawMessageProperty.toFx()) {
                            style {
                                font = Font.font("RobotoMono")
                            }
                        }
                    }
                }
            }
        }
    }.root
}
