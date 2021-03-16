package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.InfoBarFilePaper
import de.robolab.client.app.model.file.details.PathDetailBox
import de.robolab.client.app.model.file.details.PlanetStatisticsDetailBox
import de.robolab.client.app.model.file.details.PointDetailBox
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.dialog.bindStringParsing
import de.robolab.client.ui.dialog.dialogFormEntry
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.InputType
import de.westermann.kwebview.components.inputView
import de.westermann.kwebview.components.selectView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFilePaperView(
    private val viewModel: InfoBarFilePaper,
) : ViewCollection<View>() {

    private fun updateContent(box: BoxView) {
        box.clear()

        when (val content = viewModel.detailBoxProperty.value) {
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

    init {
        scrollBoxView {
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(0.5) {
                classList += "dialog-form"
                dialogFormEntry("Orientation") {
                    selectView(PreferenceStorage.paperOrientationProperty)
                }
                dialogFormEntry("Grid width") {
                    inputView(InputType.NUMBER, PreferenceStorage.paperGridWidthProperty.bindStringParsing()) {
                        min = 0.1
                        max = 1000.0
                        step = 0.001
                    }
                }
                dialogFormEntry("Paper strip width") {
                    inputView(InputType.NUMBER, PreferenceStorage.paperStripWidthProperty.bindStringParsing()) {
                        min = 0.1
                        max = 1000.0
                        step = 0.001
                    }
                }
                dialogFormEntry("Minimal padding") {
                    inputView(InputType.NUMBER, PreferenceStorage.paperMinimalPaddingProperty.bindStringParsing()) {
                        min = 0.1
                        max = 1000.0
                        step = 0.001
                    }
                }
                dialogFormEntry("Precision") {
                    inputView(InputType.NUMBER, PreferenceStorage.paperPrecisionProperty.bindStringParsing()) {
                        min = 0.0
                        max = 10.0
                        step = 1.0
                    }
                }
            }
            resizeBox(0.5) {
                viewModel.detailBoxProperty.onChange {
                    updateContent(this)
                }
                updateContent(this)
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarFilePaper
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarFilePaperView(viewModel as InfoBarFilePaper)
        }
    }
}
