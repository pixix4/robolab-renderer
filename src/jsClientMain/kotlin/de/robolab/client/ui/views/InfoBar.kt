package de.robolab.client.ui.views

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.model.file.*
import de.robolab.client.app.model.group.InfoBarGroupInfo
import de.robolab.client.app.model.group.JsonDetailBox
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.clientPosition
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import kotlinx.browser.document
import kotlin.math.max
import kotlin.math.min

class InfoBar(
    private val infoBarController: InfoBarController,
    private val infoBarActiveProperty: ObservableProperty<Boolean>,
    private val infoBarWidthProperty: ObservableProperty<Double>
) : ViewCollection<View>() {

    private val contentView: BoxView
    private val detailsView: BoxView

    private fun updateContent() {
        contentView.clear()

        val content = infoBarController.selectedContentProperty.value ?: return

        when (content) {
            is InfoBarFileEditor -> {
                contentView.initInfoBarFileEditorView(content, infoBarActiveProperty, infoBarWidthProperty)
            }
            is InfoBarTraverser -> {
                if (content.traverserProperty.value == null) {
                    content.traverse()
                }
                contentView.add(NullableViewContainer(content.traverserProperty))
            }
            is InfoBarGroupInfo -> {
                contentView.add(InfoBarGroupView(content, contentView))
            }
        }
    }

    private fun updateDetails() {
        detailsView.clear()

        val data = infoBarController.detailBoxProperty.value ?: return

        when (data) {
            is JsonDetailBox -> {
                detailsView.add(DetailBoxJson(data))
            }
            is PointDetailBox -> {
                detailsView.add(DetailBoxPoint(data))
            }
            is PathDetailBox -> {
                detailsView.add(DetailBoxPath(data))
            }
            is PlanetStatisticsDetailBox -> {
                detailsView.add(DetailBoxPlanetStatistics(data))
            }
        }
    }

    init {
        classList.bind("active", infoBarActiveProperty)

        contentView = boxView("info-bar-content") {}
        infoBarController.selectedContentProperty.onChange {
            updateContent()
        }
        updateContent()

        detailsView = boxView("info-bar-details") {}
        infoBarController.detailBoxProperty.onChange {
            updateDetails()
        }
        updateDetails()

        +ResizeView("detail-box-resize") { position, _ ->
            val height = position.y - this.offsetTopTotal
            val percent = max(0.05, min(0.95, 1.0 - (height / this.clientHeight)))
            document.body?.style?.setProperty("--detail-box-height", "${percent * 100}%")
        }

        // Close info bar on mobile
        var closePosition = Point.ZERO
        onMouseDown {
            closePosition = it.clientPosition
        }
        onClick {
            if (it.target == html && it.clientPosition.distanceTo(closePosition) < 2.0 && infoBarActiveProperty.value) {
                infoBarActiveProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}

class NullableViewContainer(private val traverserProperty: ObservableValue<TraverserBarController?>) : ViewCollection<View>() {

    private var prop: ObservableProperty<TraverserBarController>? = null
    private var view: TraverserBarView? = null

    private fun updateView() {
        val traverser = traverserProperty.value

        if (traverser == null) {
            clear()
            return
        }

        if (prop == null) {
            prop = property(traverser)
        } else {
            prop?.value = traverser
        }
        if (view == null) {
            view = TraverserBarView(prop!!)
        }

        add(view!!)
    }

    init {
        traverserProperty.onChange {
            updateView()
        }
        updateView()
    }

}
