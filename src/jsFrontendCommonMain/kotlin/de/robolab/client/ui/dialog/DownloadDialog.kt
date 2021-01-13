package de.robolab.client.ui.dialog

import de.robolab.client.ui.triggerDownloadUrl
import de.robolab.client.ui.views.utils.buttonGroup
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.*
import io.ktor.client.fetch.fetch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic

class DownloadDialog private constructor() : Dialog("Download") {

    private val downloadOverview = BoxView()

    init {
        downloadOverview.classList +="download-overview"
        tab {
            + downloadOverview
        }

        artefactMapProperty.onChange.now {
            reload()
        }
    }

    private fun reload() {
        with(downloadOverview) {
            clear()
            for ((os, archMap) in artefactMap.toList().sortedByDescending { it.first }) {
                boxView {
                    classList += os
                    when(os) {
                        "windows" -> imageView("public/images/windows.svg")
                        "mac" -> imageView("public/images/mac.svg")
                        "linux" -> imageView("public/images/linux.svg")
                        else -> textView(os)
                    }
                    boxView {
                        for ((arch, artefactList) in archMap.toList().sortedByDescending { it.first }) {
                            textView(arch)
                            buttonGroup {
                                classList += "button-form-group"
                                for (artefact in artefactList) {
                                    button(artefact.format) {
                                        title = artefact.filename
                                        onClick {
                                            triggerDownloadUrl(artefact.filename, artefact.url)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Serializable
    data class Artefact(
        val url: String,
        val filename: String,
        val os: String,
        val arch: String,
        val format: String,
        val version: String,
    )

    companion object {
        private val artefactMapProperty=  property<Map<String, Map<String, List<Artefact>>>>(emptyMap())
        private var artefactMap by artefactMapProperty

        private suspend fun update() {
            try {
                val result = fetch("download/latest.json").await().json().await()
                @Suppress("EXPERIMENTAL_API_USAGE")
                artefactMap = Json.decodeFromDynamic(result)
            } catch (e: Exception) {
                artefactMap = emptyMap()
            }
        }

        fun open() {
            GlobalScope.launch {
                update()
            }
            open(DownloadDialog())
        }

        fun init(block: () -> Unit) {
            GlobalScope.launch {
                update()
                if (artefactMap.isNotEmpty()) {
                    block()
                }
            }
        }
    }
}
