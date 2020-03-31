package de.robolab.web

import de.robolab.app.Main
import de.robolab.web.adapter.WebCanvas
import de.westermann.kwebview.async
import de.westermann.kwebview.components.*
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.browser.window

fun main() {
    init {
        clear()

        val canvas = Canvas()
        val webCanvas = WebCanvas(canvas)

        val main = Main(webCanvas)

        if (window.localStorage["theme"] == "DARK") {
            main.darkThemeProperty.value = true
        }
        main.themeProperty.onChange {
            window.localStorage["theme"] = main.themeProperty.value.name
        }

        boxView("toolbar") {
            val animateBox = checkbox(main.animateProperty)
            label(animateBox, "Animate")

            val editableBox = checkbox(main.editableProperty)
            label(editableBox, "Editable") {
                classList += "group"
            }

            textView("|") {
                classList += "divider"
            }

            val lightThemeBox = checkbox(main.lightThemeProperty)
            label(lightThemeBox, "Light theme")
            lightThemeBox.checkedProperty.onChange {
                lightThemeBox.checked = main.lightThemeProperty.value
            }

            val darkThemeBox = checkbox(main.darkThemeProperty)
            label(darkThemeBox, "Dark theme"){
                classList += "group"
            }
            darkThemeBox.checkedProperty.onChange {
                darkThemeBox.checked = main.darkThemeProperty.value
            }

            textView("|") {
                classList += "divider"
            }

            button("Export SVG") {
                onClick {
                    triggerDownload("export.svg", main.exportSVG())
                }
            }
            button("Export PNG") {
                onClick {
                    val dimension = main.exportGetSize()
                    val exportCanvas = Canvas()
                    exportCanvas.updateSize(dimension.width.toInt(), dimension.height.toInt(), Main.EXPORT_SCALE)

                    val exportWebCanvas = WebCanvas(exportCanvas)

                    main.exportRender(exportWebCanvas)

                    triggerDownloadPNG("export.png", exportCanvas)
                }
            }
        }
        boxView("main") {
            add(canvas)
        }
        val statusBar = boxView("statusbar")
        updateStatusBar(statusBar, main.pointerProperty.value)
        main.pointerProperty.onChange {
            updateStatusBar(statusBar, main.pointerProperty.value)
        }

        async {
            canvas.updateSize()
        }
    }
}

fun updateStatusBar(statusBar: BoxView, content: List<String>) {
    statusBar.clear()
    for (item in content) {
        statusBar.textView(item)
    }
}
