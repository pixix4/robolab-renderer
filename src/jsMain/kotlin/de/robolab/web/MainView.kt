package de.robolab.web

import de.robolab.app.Main
import de.robolab.web.adapter.WebCanvas
import de.westermann.kwebview.async
import de.westermann.kwebview.components.*

fun main(args: Array<String>) {
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
        boxView("statusbar") {
            textView(main.pointerProperty)
        }

        async {
            canvas.updateSize()
        }
    }
}
