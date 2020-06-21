package de.robolab.client.ui.view

import de.robolab.client.app.model.file.PointDetailBox
import tornadofx.*

class DetailBoxPoint(private val data: PointDetailBox) : View() {

    override val root = vbox {
        form {
            fieldset("Point") {
                field("Position") {
                    label(data.position)
                }
                field("Hidden") {
                    checkbox {
                        isSelected = data.isHidden
                        isDisable = true
                    }
                }
            }
            if (data.targetsSend.isNotEmpty()) {
                fieldset("Targets send" + data.targetsSend.size.let { if (it > 1) " ($it)" else "" }) {
                    for (str in data.targetsSend) {
                        field {
                            label(str)
                        }
                    }
                }
            }
            if (data.targetExposedAt.isNotEmpty()) {
                fieldset("Target exposed at" + data.targetExposedAt.size.let { if (it > 1) " ($it)" else "" }) {
                    for (str in data.targetExposedAt) {
                        field {
                            label(str)
                        }
                    }
                }
            }
            if (data.pathSend.isNotEmpty()) {
                fieldset("Path send" + data.pathSend.size.let { if (it > 1) " ($it)" else "" }) {
                    for (str in data.pathSend) {
                        field {
                            label(str)
                        }
                    }
                }
            }
        }
    }
}
