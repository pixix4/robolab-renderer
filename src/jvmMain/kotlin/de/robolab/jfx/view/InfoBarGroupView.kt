package de.robolab.jfx.view

import com.soywiz.klock.format
import de.robolab.app.model.group.InfoBarGroupInfo
import de.robolab.communication.RobolabMessage
import de.robolab.jfx.adapter.toFx
import javafx.scene.layout.Priority
import tornadofx.*

class InfoBarGroupView(private val content: InfoBarGroupInfo) : View() {

    override val root = vbox {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        form {
            fieldset("Attempt info") {
                field("Messages") {
                    label(content.messageCountStringProperty.toFx())
                }
                field("First message") {
                    label(content.firstMessageTimeStringProperty.toFx())
                }
                field("Last message") {
                    label(content.lastMessageTimeStringProperty.toFx())
                }
                field("Attempt duration") {
                    label(content.attemptDurationStringProperty.toFx())
                }
            }
        }

        tableview(content.messages.toFx()) {
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
                val index = content.messages.indexOf(it ?: return@onSelectionChange)
                content.selectedIndexProperty.value = index
            }
            selectionModel.selectedIndexProperty().onChange {
                content.selectedIndexProperty.value = it
            }
        }
    }
}
