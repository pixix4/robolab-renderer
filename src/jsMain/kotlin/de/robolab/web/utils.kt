package de.robolab.web

import de.westermann.kwebview.components.Canvas
import org.w3c.dom.HTMLElement
import kotlin.browser.document


// external fun decodeURIComponent(encodedURI: String): String
external fun encodeURIComponent(encodedURI: String): String
fun triggerDownload(filename: String, content: String) {
    val element = document.createElement("a") as HTMLElement
    element.setAttribute("href", "data:application/jsoncharset=utf-8," + encodeURIComponent(content))
    element.setAttribute("download", filename)

    element.style.display = "none"
    document.body?.appendChild(element)

    element.click()

    document.body?.removeChild(element)
}

fun triggerDownloadPNG(filename: String, canvas: Canvas) {
    var img = canvas.html.toDataURL("image/png")
    img = img.replace("^data:image/[^;]*".toRegex(), "data:application/octet-stream")

    val element = document.createElement("a") as HTMLElement
    element.setAttribute("href", img)
    element.setAttribute("download", filename)

    element.style.display = "none"
    document.body?.appendChild(element)

    element.click()

    document.body?.removeChild(element)
}