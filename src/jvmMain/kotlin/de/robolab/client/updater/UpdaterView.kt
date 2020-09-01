package de.robolab.client.updater

import javafx.application.Platform
import tornadofx.*
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class UpdaterView : View() {

    override val root = hbox {
        titleProperty.value = "Robolab Renderer - Updater"

        val params = primaryStage.properties["params"] as? List<*> ?: exitProcess(1)

        minWidth = 400.0
        style {
            padding = box(2.em)
        }

        val sourceUrl = params[0] as? String ?: exitProcess(1)
        val targetFile = params[1] as? String ?: exitProcess(1)

        Platform.runLater {
            requestFocus()
        }

        val updateBarView = UpdateBarView()

        add(updateBarView)

        thread {
            val target = File(targetFile)

            val tempFile = File.createTempFile("update", ".jar")

            println("Download $sourceUrl")
            Downloader.download(sourceUrl, tempFile, updateBarView)
            println()

            tempFile.copyTo(target, overwrite = true)

            val vmArguments = ManagementFactory
                .getRuntimeMXBean()
                .inputArguments
                .filter { !it.contains("-agentlib") }
                .toTypedArray()

            val runProcessBuilder = ProcessBuilder().command(
                System.getProperty("java.home") + "/bin/java",
                *vmArguments,
                "-jar",
                target.absolutePath,
            ).inheritIO()

            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                try {
                    runProcessBuilder.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })

            exitProcess(0)
        }
    }

    override fun onUndock() {
        exitProcess(0)
    }

    override fun onDock() {
        super.onDock()

        primaryStage.requestFocus()
    }
}
