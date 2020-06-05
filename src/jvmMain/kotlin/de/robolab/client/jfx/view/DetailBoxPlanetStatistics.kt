package de.robolab.client.jfx.view

import de.robolab.client.app.model.file.PlanetStatisticsDetailBox
import de.robolab.client.jfx.adapter.toFx
import tornadofx.*

class DetailBoxPlanetStatistics(private val data: PlanetStatisticsDetailBox) : View() {

    override val root = vbox {
        form {
            for ((label, block) in data.data) {
                fieldset(label) {
                    for ((key, value) in block) {
                        field(key) {
                            label(value.toFx())
                        }
                    }
                }
            }
        }
    }
}
