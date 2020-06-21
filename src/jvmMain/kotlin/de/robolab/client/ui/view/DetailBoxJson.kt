package de.robolab.client.ui.view

import de.robolab.client.app.model.group.JsonDetailBox
import javafx.scene.text.Font
import tornadofx.*

class DetailBoxJson(private val data: JsonDetailBox) : View() {

    override val root = vbox {
        form {
            fieldset(data.header) {
                field("From") {
                    label(data.from)
                }
                field("Group") {
                    label(data.topic)
                }
                field("Topic") {
                    label(data.topic)
                }
                field("Time") {
                    label(data.time)
                }
            }
            fieldset("Content") {
                field("Details") {
                    label(data.details)
                }
                field("Message") {
                    label(data.rawMessage) {
                        style {
                            font = Font.font("RobotoMono")
                        }
                    }
                }
            }
        }
    }
}
