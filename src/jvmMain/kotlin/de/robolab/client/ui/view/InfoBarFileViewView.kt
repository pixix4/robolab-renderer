package de.robolab.client.ui.view

import de.robolab.client.app.model.file.InfoBarFileView
import de.robolab.client.app.model.file.PathDetailBox
import de.robolab.client.app.model.file.PlanetStatisticsDetailBox
import de.robolab.client.app.model.file.PointDetailBox
import de.robolab.common.parser.isLineValid
import javafx.scene.layout.VBox
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import tornadofx.*
import java.util.*
import java.util.regex.Pattern

/**
 * @author lars
 */
@Suppress("RedundantLambdaArrow")
class InfoBarFileViewView(private val content: InfoBarFileView) : View() {

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
        scrollBox(1.0) {
            content.detailBoxProperty.onChange {
                updateContent(this)
            }
            updateContent(this)
        }
    }.root
}
