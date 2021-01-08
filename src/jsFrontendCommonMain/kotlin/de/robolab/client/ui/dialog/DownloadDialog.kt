package de.robolab.client.ui.dialog

import de.robolab.client.ui.triggerDownloadUrl
import de.robolab.client.ui.views.utils.buttonGroup
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.textView
import io.ktor.client.fetch.fetch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic

class DownloadDialog private constructor() : Dialog("Download") {

    init {
        tab {
            GlobalScope.launch {
                val result = fetch("download/latest.json").await().json().await()
                @Suppress("EXPERIMENTAL_API_USAGE") val map = Json.decodeFromDynamic<Map<String, Map<String, List<Artefact>>>>(result)

                boxView("download-overview") {
                    for ((os, archMap) in map.toList().sortedByDescending { it.first }) {
                        boxView {
                            textView(os)
                            boxView {
                                for ((arch, artefactList) in archMap) {
                                    textView(arch)
                                    buttonGroup {
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
        fun open() {
            open(DownloadDialog())
        }
    }
}
