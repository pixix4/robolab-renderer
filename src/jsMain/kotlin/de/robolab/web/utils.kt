package de.robolab.web

import de.robolab.renderer.theme.Theme
import de.robolab.utils.PreferenceStorage
import de.westermann.kwebview.components.Canvas
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window


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

fun watchSystemTheme() {
    val schemaLightQuery = window.matchMedia("(prefers-color-scheme: light)")
    val schemaDarkQuery = window.matchMedia("(prefers-color-scheme: dark)")
    val schemaNoPreferenceQuery = window.matchMedia("(prefers-color-scheme: no-preference)")

    fun update() {
        if (PreferenceStorage.useSystemTheme) {
            if (schemaDarkQuery.matches) {
                PreferenceStorage.selectedTheme = Theme.DARK
            } else if (schemaLightQuery.matches) {
                PreferenceStorage.selectedTheme = Theme.LIGHT
            }
        }
    }

    fun eventListener(@Suppress("UNUSED_PARAMETER") event: Event) {
        update()
    }

    PreferenceStorage.useSystemThemeProperty.onChange {
        update()
    }

    schemaLightQuery.addListener(::eventListener)
    schemaDarkQuery.addListener(::eventListener)
    schemaNoPreferenceQuery.addListener(::eventListener)

    update()
}
