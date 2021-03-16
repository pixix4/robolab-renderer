package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.SystemController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.adapter.WebCanvas
import de.robolab.client.utils.PreferenceStorage
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class SideBarNavigationView(private val viewModel: INavigationBarList) : ViewCollection<View>() {



    init {
        boxView {
            listFactory(viewModel.childrenProperty, {
                SideBarNavigationEntryView(it)
            })

            onWheel {
                updatePreviewPosition()
            }
        }

        initPreviewView()
    }

    companion object : ViewFactory {
        const val PREVIEW_PADDING = 20.0

        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is INavigationBarList
        }

        override fun create(viewModel: ViewModel): View {
            return SideBarNavigationView(viewModel as INavigationBarList)
        }


        private var previewView: View? = null
        private var previewDimension = Dimension.ZERO
        private val previewBox = BoxView()
        private val previewImage = ImageView("")
        private var isPreviewInitialized = false

        private fun updatePreviewPosition() {
            val rectangle = previewView?.dimension ?: return

            val leftPx = rectangle.right - 10.0

            val t0 = (rectangle.top + rectangle.bottom) / 2 - previewDimension.height / 2
            val topPx = max(
                PREVIEW_PADDING,
                min(window.innerHeight - previewDimension.height - PREVIEW_PADDING, t0)
            )

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

        fun initPreviewView() {
            if (isPreviewInitialized) return
            isPreviewInitialized = true

            previewImage.html.draggable = false
            Body += previewBox
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
        }
    }

    inner class SideBarNavigationEntryView(entry: INavigationBarEntry) :
        ViewCollection<View>() {

        private fun calculatePreviewDimension(dimension: Dimension): Pair<Dimension, Double> {
            val dpi = window.devicePixelRatio
            val dpiDimension = dimension * dpi
            val windowDimension = Dimension(window.innerWidth, window.innerHeight) - Dimension(
                PREVIEW_PADDING * 2,
                PREVIEW_PADDING * 2
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
            classList.bind("one-line", entry.subtitleProperty.mapBinding { it.isBlank() })

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
                    SystemController.openContextMenu(menu)
                }
            }

            var isHovered: Boolean
            var previewSrc = ""
            var previewDimension = Dimension.ZERO
            var previewTimestamp = -1L
            PreferenceStorage.selectedThemeProperty.onChange {
                previewTimestamp = -1L
            }
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
                                renderPreview(previewSrc, previewDimension, this@SideBarNavigationEntryView)
                            }
                        }
                    } else {
                        if (previewSrc.isNotEmpty()) {
                            renderPreview(previewSrc, previewDimension, this@SideBarNavigationEntryView)
                        }
                    }
                }
            }

            onMouseLeave {
                isHovered = false
                if (it.clientPosition !in dimension.shrink(1.0)) {
                    removePreview()
                }
            }
        }
    }
}
