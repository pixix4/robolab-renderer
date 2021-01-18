package de.robolab.client.ui.views

import com.soywiz.klock.DateTime
import de.robolab.client.app.controller.FileImportController
import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.ui.adapter.WebCanvas
import de.robolab.client.ui.lineSequence
import de.robolab.client.ui.openFile
import de.robolab.client.ui.pathOrName
import de.robolab.client.ui.readText
import de.robolab.client.utils.electron
import de.robolab.client.utils.noElectron
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.westermann.kobserve.event.now
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.clientPosition
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class NavigationBar(
    private val navigationBarController: NavigationBarController,
    private val fileImportController: FileImportController,
    private val uiController: UiController
) : ViewCollection<View>() {

    private fun createEntry(entry: INavigationBarEntry) = NavigationBarEntry(entry, this)

    private val previewBox = BoxView()
    private val previewImage = ImageView("")
    private var previewView: View? = null
    private var previewDimension = Dimension.ZERO

    private fun BoxView.setupTabs() {
        clear()

        for ((index, tab) in navigationBarController.tabListProperty.value.withIndex()) {
            boxView("tab-bar-item") {
                classList.bind("active", navigationBarController.tabProperty.mapBinding { it == tab })

                iconView(tab.iconProperty)
                tab.nameProperty.onChange.now {
                    title = tab.nameProperty.value
                }

                onClick {
                    navigationBarController.tabIndexProperty.value = index
                }
            }
        }
    }

    private fun updatePreviewPosition() {
        val rectangle = previewView?.dimension ?: return

        val leftPx = rectangle.right - 10.0

        val t0 = (rectangle.top + rectangle.bottom) / 2 - previewDimension.height / 2
        val topPx = max(
            PREVIEW_PADDING,
            min(window.innerHeight - previewDimension.height - PREVIEW_PADDING, t0)
        ) - offsetTopTotal

        previewBox.style {
            left = "${leftPx}px"
            top = "${topPx}px"
        }
    }

    fun renderPreview(src: String, dimension: Dimension, referenceView: View) {
        previewView = referenceView
        previewDimension = dimension
        previewImage.source = src
        previewBox.style {
            width = "${dimension.width}px"
            height = "${dimension.height}px"
            display = "block"
        }
        updatePreviewPosition()
    }

    fun removePreview() {
        previewView = null
        previewDimension = Dimension.ZERO
        previewImage.source = ""
        previewBox.style {
            removeProperty("left")
            removeProperty("top")
            removeProperty("width")
            removeProperty("height")
            display = "none"
        }
    }

    init {
        boxView("navigation-bar-header", "tab-bar") {
            navigationBarController.tabListProperty.onChange.now {
                setupTabs()
            }
        }
        boxView("navigation-bar-content") {
            boxView("navigation-bar-group-head") {
                val labelText = navigationBarController.backButtonLabelProperty

                iconView(MaterialIcon.ARROW_BACK)
                textView(labelText)
                title = labelText.value
                labelText.onChange {
                    title = labelText.value
                }

                classList.bind("active", navigationBarController.backButtonEnabledProperty)

                onClick {
                    navigationBarController.onBackButtonClick()
                }
            }
            boxView("navigation-bar-list") {
                listFactory(navigationBarController.entryListProperty, this@NavigationBar::createEntry)

                onWheel {
                    updatePreviewPosition()
                }
            }
            boxView("navigation-bar-empty") {
                textView("Nothing to show!")
            }
        }

        boxView("navigation-bar-search", "button-group", "button-form-group") {
            val searchView = inputView(InputType.SEARCH, navigationBarController.searchStringProperty) {
                placeholder = "Search…"

                onKeyDown {
                    if (it.keyCode == 13) {
                        navigationBarController.submitSearch()
                    }
                }
            }
            iconView(MaterialIcon.CANCEL) {
                onClick {
                    it.preventDefault()
                    it.stopPropagation()
                    navigationBarController.searchStringProperty.value = ""
                    searchView.focus()
                }
            }

            button {
                iconView(MaterialIcon.PUBLISH)

                onClick {
                    GlobalScope.launch(Dispatchers.Main) {
                        val files =
                            openFile(*fileImportController.supportedFileTypes.toTypedArray())

                        for (file in files) {
                            val content = file.readText()
                            if (content != null) {
                                fileImportController.importFile(
                                    file.pathOrName(),
                                    DateTime(file.lastModified.toLong())
                                ) {
                                    file.lineSequence()
                                }
                            }
                        }
                    }
                }
            }
        }

        previewImage.html.draggable = false
        +previewBox
        with(previewBox) {
            classList += "navigation-bar-preview"
            +previewImage

            onMouseLeave {
                val rect = previewView?.dimension
                if (rect != null && it.clientPosition !in rect.shrink(1.0)) {
                    removePreview()
                }
                removePreview()
            }
        }

        // Close navigation bar on mobile
        onClick {
            if (it.target == html && uiController.navigationBarEnabledProperty.value) {
                uiController.navigationBarEnabledProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }

    companion object {
        const val PREVIEW_PADDING = 20.0
    }
}

class NavigationBarEntry(entry: INavigationBarEntry, navigationBar: NavigationBar) :
    ViewCollection<View>() {

    private fun calculatePreviewDimension(dimension: Dimension): Pair<Dimension, Double> {
        val dpi = window.devicePixelRatio
        val dpiDimension = dimension * dpi
        val windowDimension = Dimension(window.innerWidth, window.innerHeight) - Dimension(
            NavigationBar.PREVIEW_PADDING * 2,
            NavigationBar.PREVIEW_PADDING * 2
        )
        val maxDimension = Dimension(400.0, 200.0).min(windowDimension)

        val widthScale = maxDimension.width / dpiDimension.width
        val heightScale = maxDimension.height / dpiDimension.height
        val minScale = min(widthScale, heightScale)

        return dpiDimension * minScale to dpi
    }

    init {
        textView(entry.nameProperty)
        textView(entry.subtitleProperty)
        boxView {
            entry.statusIconProperty.onChange.now {
                clear()
                for (icon in entry.statusIconProperty.value) {
                    iconView(icon)
                }
            }
        }

        classList.bind("disabled", !entry.enabledProperty)

        onClick {
            entry.open(it.ctrlKey || it.metaKey)
        }
        onAuxClick { event ->
            val which = event.asDynamic().which as Int
            if (which == 2) {
                event.preventDefault()
                entry.open(true)
            }
        }

        onContext { event ->
            event.stopPropagation()
            event.preventDefault()

            val menu = entry.generateContextMenuAt(Point(event.clientX, event.clientY))
            if (menu != null) {
                electron { electron ->
                    electron.menu(menu)
                }
                noElectron {
                    ContextMenuView.open(menu)
                }
            }
        }

        var isHovered: Boolean
        var previewSrc = ""
        var previewDimension = Dimension.ZERO
        var previewTimestamp = -1L
        onMouseEnter {
            isHovered = true

            GlobalScope.launch {
                val currentTimestamp = entry.getRenderDataTimestamp()
                if (currentTimestamp > previewTimestamp) {
                    previewTimestamp = currentTimestamp

                    var dim = Dimension.ZERO
                    val webCanvas = entry.renderPreview { dimension ->
                        val (d, s) = calculatePreviewDimension(dimension)
                        dim = d
                        WebCanvas(d, s)
                    }

                    if (webCanvas != null) {
                        previewDimension = dim
                        previewSrc = webCanvas.canvas.html.toDataURL("image/png")

                        if (isHovered && previewSrc.isNotEmpty()) {
                            navigationBar.renderPreview(previewSrc, previewDimension, this@NavigationBarEntry)
                        }
                    }
                } else {
                    if (previewSrc.isNotEmpty()) {
                        navigationBar.renderPreview(previewSrc, previewDimension, this@NavigationBarEntry)
                    }
                }
            }
        }

        onMouseLeave {
            isHovered = false
            if (it.clientPosition !in dimension.shrink(1.0)) {
                navigationBar.removePreview()
            }
        }
    }
}
