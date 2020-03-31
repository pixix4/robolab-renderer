package de.robolab.web

import de.robolab.app.Main
import de.robolab.web.adapter.WebCanvas
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.async
import de.westermann.kwebview.components.*

fun main() {
    init {
        clear()

        val canvas = Canvas()
        val webCanvas = WebCanvas(canvas)

        val main = Main(webCanvas)

        boxView("toolbar") {
            val animateBox = checkbox(main.animateProperty)
            label(animateBox, "Animate")

            val editableBox = checkbox(main.editableProperty)
            label(editableBox, "Editable")

            button("Export SVG") {
                onClick {
                    val export = main.exportSVG()

                    if (export != null) {
                        triggerDownload("export.svg", export)
                    }
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
