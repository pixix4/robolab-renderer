package de.robolab.client.ui.view.boxes

import de.robolab.client.app.model.file.PathDetailBox
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.dialog.IntStringConverter
import tornadofx.*

class DetailBoxPath(private val data: PathDetailBox) : View() {

    override val root = vbox {
        form {
            fieldset("Path") {
                field("Source") {
                    label(data.source)
                }
                field("Target") {
                    label(data.target)
                }
                field("Weight") {
                    textfield(data.weightProperty.toFx(), IntStringConverter(1))
                }
                field("Hidden") {
                    checkbox(null, data.isHiddenProperty.toFx())
                }
                field("Length") {
                    label(data.length)
                }
                field("Classifier") {
                    label(data.classifier)
                }
            }

            if (data.pathExposedAt.isNotEmpty()) {
                fieldset("Path exposed at" + data.pathExposedAt.size.let { if (it > 1) " ($it)" else "" }) {
                    for (str in data.pathExposedAt) {
                        field {
                            label(str)
                        }
                    }
                }
            }
        }
    }
}
