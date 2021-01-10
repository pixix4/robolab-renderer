package de.robolab.client.ui

import de.robolab.client.utils.PreferenceStorage
import de.westermann.kwebview.components.Canvas
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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

fun triggerDownloadUrl(filename: String, url: String) {
    val element = document.createElement("a") as HTMLElement
    element.setAttribute("href", url)
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

suspend fun openFile(vararg accept: String): List<File> = suspendCoroutine { continuation ->
    val fileInput = document.createElement("input") as HTMLInputElement
    fileInput.type = "file"
    if (accept.isNotEmpty()) {
        fileInput.accept = accept.joinToString(",")
    }

    fileInput.onchange = { event ->
        val target = event.target
        if (target != null && target is HTMLInputElement) {
            val files = target.files?.let { fileList ->
                (0 until fileList.length).mapNotNull { fileList.item(it) }
            } ?: emptyList()

            continuation.resume(files)
        }
    }
    fileInput.onabort = {
        continuation.resume(emptyList())
    }

    fileInput.style.display = "none"
    document.body?.appendChild(fileInput)

    fileInput.click()

    document.body?.removeChild(fileInput)
}

fun File.pathOrName(): String {
    if (js("File.prototype.hasOwnProperty('path')").unsafeCast<Boolean>()) {
        return this.asDynamic().path.unsafeCast<String>()
    }

    return name
}

suspend fun File.readText(): String? = suspendCoroutine { continuation ->
    val reader = FileReader()

    reader.onload = {
        continuation.resume(reader.result as? String)
    }

    reader.onerror = {
        continuation.resume(null)
    }

    reader.readAsText(this)
}

suspend fun File.lineSequence(): Sequence<String> {
    val text = readText() ?: ""
    return text.splitToSequence('\n')
}

fun watchSystemTheme() {
    val schemaLightQuery = window.matchMedia("(prefers-color-scheme: light)")
    val schemaDarkQuery = window.matchMedia("(prefers-color-scheme: dark)")
    val schemaNoPreferenceQuery = window.matchMedia("(prefers-color-scheme: no-preference)")

    fun update() {
        if (PreferenceStorage.useSystemTheme) {
            if (schemaDarkQuery.matches) {
                PreferenceStorage.selectedTheme = PreferenceStorage.selectedTheme.getThemeByMode(true)
            } else if (schemaLightQuery.matches) {
                PreferenceStorage.selectedTheme = PreferenceStorage.selectedTheme.getThemeByMode(false)
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

val KeyboardEvent.ctrlOrCommandKey
    get() = ctrlKey || metaKey
