package de.robolab.client.ui.dialog

import de.robolab.client.ui.adapter.toFx
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.UpdateChannel
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.BuildInformationFile
import de.robolab.common.utils.Version
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import io.ktor.client.*
import io.ktor.client.request.*
import javafx.application.Platform
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class UpdateDialog : GenericDialog() {

    private lateinit var container: VBox

    override val root = buildContent("Update") {
        vgrow = Priority.ALWAYS
        container = vbox {
            vgrow = Priority.ALWAYS

            form {
                fieldset {
                    field("Current version") {
                        textfield(localVersionProperty.mapBinding { it.toString() }.toFx()) {
                            isEditable = false
                        }
                    }
                    field("Remote version") {
                        textfield(remoteVersionProperty.mapBinding { it?.toString() ?: "Loading…" }.toFx()) {
                            isEditable = false
                        }
                    }
                    field(forceLabelIndent = true) {
                        button("Update") {
                            enableWhen(updateAvailable.toFx())
                            setOnAction {
                                startUpdate()
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        GlobalScope.launch(Dispatchers.IO) {
            checkUpdate()
        }
    }

    private fun startUpdate() {
        performUpdate()
    }

    companion object {

        val isUpdateAllowed = System
            .getProperty("sun.java.command")
            .split(" ")
            .firstOrNull()
            ?.endsWith(".jar") == true

        fun open() {
            open<UpdateDialog>()
        }

        fun openIfUpdateAvailable() {
            if (!isUpdateAllowed) return

            GlobalScope.launch(Dispatchers.IO) {
                checkUpdate()

                Platform.runLater {
                    if (autoUpdateAvailable.value) {
                        open()
                    }
                }
            }
        }

        private const val remoteUrl = "https://robolab.pixix4.com/jvm/"
        private const val remoteVersionFile = remoteUrl + "build.ini"
        private const val remoteJarFile = remoteUrl + "robolab-renderer.jar"

        private val localVersionProperty = BuildInformation.versionClientProperty
        private val remoteVersionProperty = de.westermann.kobserve.property.property<Version>()
        private val statusProperty = de.westermann.kobserve.property.property(Status.UNKNOWN)

        val updateAvailable = statusProperty.mapBinding {
            isUpdateAllowed && (it == Status.NEW_SNAPSHOT || it == Status.NEW_VERSION)
        }

        val autoUpdateAvailable = statusProperty
            .join(PreferenceStorage.autoUpdateChannelProperty) { status, channel ->
                isUpdateAllowed && ((status == Status.NEW_VERSION && channel != UpdateChannel.NEVER) ||
                        (status == Status.NEW_SNAPSHOT && channel == UpdateChannel.NIGHTLY))
            }

        enum class Status {
            UNKNOWN,
            UP_TO_DATE,
            NEW_SNAPSHOT,
            NEW_VERSION
        }

        suspend fun checkUpdate(): Status {
            val status = try {
                val remoteFile = HttpClient().get<String>(remoteVersionFile)
                val remoteBuildInformation = BuildInformationFile(remoteFile)

                var remoteVersion = remoteBuildInformation.versionClient
                val remoteCommitCount = remoteBuildInformation.vcsCommitCount

                val localVersion = localVersionProperty.value
                val localCommitCount = BuildInformation.vcsCommitCount

                val commitCountDiff = remoteCommitCount - localCommitCount
                if (commitCountDiff > 0) {
                    remoteVersion = remoteVersion.copy(metadata = commitCountDiff.toString())
                }

                var remoteVersionChanged = false
                if (remoteVersionProperty.value != remoteVersion) {
                    remoteVersionProperty.value = remoteVersion
                    remoteVersionChanged = true
                }

                if (remoteVersion > localVersion) {
                    if (remoteVersionChanged) {
                        println("New version available $remoteVersion")
                    }
                    Status.NEW_VERSION
                } else if (remoteVersion == localVersion && commitCountDiff > 0) {
                    if (remoteVersionChanged) {
                        println("New snapshot available $localVersion (+$commitCountDiff commits)")
                    }
                    Status.NEW_SNAPSHOT
                } else {
                    Status.UP_TO_DATE
                }
            } catch (e: Exception) {
                Status.UNKNOWN
            }

            Platform.runLater {
                statusProperty.value = status
            }
            return status
        }

        private fun performUpdate() {
            val currentFile = File(System.getProperty("java.class.path"))
            val updaterFile = File.createTempFile("updater", ".jar")

            extractUpdater(updaterFile)

            startUpdater(updaterFile, currentFile)
        }

        private fun extractUpdater(target: File) {
            val stream = this::class.java.classLoader.getResourceAsStream("updater.jar") ?: return
            stream.transferTo(target.outputStream())
            Thread.sleep(100L)
        }

        private fun startUpdater(updater: File, currentFile: File) {
            val vmArguments = ManagementFactory
                .getRuntimeMXBean()
                .inputArguments
                .filter { !it.contains("-agentlib") }
                .toTypedArray()

            val updateProcessBuilder = ProcessBuilder().command(
                System.getProperty("java.home") + "/bin/java",
                *vmArguments,
                "-jar",
                updater.absolutePath,
                "--url",
                remoteJarFile,
                "--file",
                currentFile.absolutePath,
            ).inheritIO()

            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                try {
                    updateProcessBuilder.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })

            exitProcess(0)
        }
    }
}
