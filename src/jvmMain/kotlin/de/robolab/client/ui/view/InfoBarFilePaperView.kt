package de.robolab.client.ui.view

import de.robolab.client.app.model.file.*
import de.robolab.client.renderer.drawable.edit.PaperBackgroundDrawable
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.dialog.DoubleStringConverter
import de.robolab.client.ui.dialog.IntStringConverter
import de.robolab.client.utils.PreferenceStorage
import javafx.scene.layout.VBox
import tornadofx.*

/**
 * @author lars
 */
@Suppress("RedundantLambdaArrow")
class InfoBarFilePaperView(private val content: InfoBarFilePaper) : View() {

    private fun updateContent(box: VBox) {
        box.clear()

        when (val content = content.detailBoxProperty.value) {
            is PlanetStatisticsDetailBox -> {
                box.add(DetailBoxPlanetStatistics(content))
            }
            is PathDetailBox -> {
                box.add(DetailBoxPath(content))
            }
            is PointDetailBox -> {
                box.add(DetailBoxPoint(content))
            }
        }
    }

    override val root = scrollBoxView {
        scrollBox(0.5) {
            form {
                fieldset {
                    field("Paper orientation") {
                        combobox(
                            PreferenceStorage.paperOrientationProperty.toFx(),
                            PaperBackgroundDrawable.Orientation.values().toList()
                        )
                    }
                    field("Grid width") {
                        textfield(
                            PreferenceStorage.paperGridWidthProperty.toFx(),
                            DoubleStringConverter(PreferenceStorage.paperGridWidthProperty.default)
                        )
                    }
                    field("Paper strip width") {
                        textfield(
                            PreferenceStorage.paperStripWidthProperty.toFx(),
                            DoubleStringConverter(PreferenceStorage.paperStripWidthProperty.default)
                        )
                    }
                    field("Minimal padding") {
                        textfield(
                            PreferenceStorage.paperMinimalPaddingProperty.toFx(), DoubleStringConverter(
                                PreferenceStorage.paperMinimalPaddingProperty.default
                            )
                        )
                    }
                    field("Precision") {
                        textfield(
                            PreferenceStorage.paperPrecisionProperty.toFx(),
                            IntStringConverter(PreferenceStorage.paperPrecisionProperty.default)
                        )
                    }
                }
            }
        }
        scrollBox(0.5) {
            content.detailBoxProperty.onChange {
                updateContent(this)
            }
            updateContent(this)
        }
    }.root
}
