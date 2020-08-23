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
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.*
import tornadofx.*
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.roundToInt
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
        container.clear()

        val view = ProgressBarListenerView()

        container += view

        thread {
            downloadUpdate(view)
            performUpdate()
        }
    }

    companion object {
        fun open() {
            open<UpdateDialog>()
        }

        fun openIfUpdateAvailable() {
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
            it == Status.NEW_SNAPSHOT || it == Status.NEW_VERSION
        }

        val autoUpdateAvailable = statusProperty
            .join(PreferenceStorage.autoUpdateChannelProperty) { status, channel ->
                (status == Status.NEW_VERSION && channel != UpdateChannel.NEVER) ||
                        (status == Status.NEW_SNAPSHOT && channel == UpdateChannel.NIGHTLY)
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

                val remoteVersion = remoteBuildInformation.versionClient
                remoteVersionProperty.value = remoteVersion
                val remoteCommitCount = remoteBuildInformation.vcsCommitCount

                val localVersion = localVersionProperty.value
                val localCommitCount = BuildInformation.vcsCommitCount

                val commitCountDiff = remoteCommitCount - localCommitCount

                if (remoteVersion > localVersion) {
                    println("New version available $remoteVersion")
                    Status.NEW_VERSION
                } else if (remoteVersion == localVersion && commitCountDiff > 0) {
                    println("New snapshot available $localVersion (+$commitCountDiff commits)")
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

        private fun downloadUpdate(listener: ProgressListener) {
            val currentFile = File(System.getProperty("java.class.path"))
            val updateFile = currentFile.resolveSibling("update.jar")

            val client = OkHttpClient.Builder()
                .addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val originalResponse: Response = chain.proceed(chain.request())
                    originalResponse.newBuilder()
                        .body(ProgressResponseBody(originalResponse.body!!, listener))
                        .build()
                })
                .build()

            val request = Request.Builder()
                .url(remoteJarFile)
                .build()

            client.newCall(request).execute().use { response ->
                val stream = response.body?.byteStream() ?: return
                updateFile.outputStream().use { out ->
                    stream.copyTo(out)
                }
            }
        }

        private fun performUpdate() {
            val currentFile = File(System.getProperty("java.class.path"))
            val updateFile = currentFile.resolveSibling("update.jar")
            val backupFile = currentFile.resolveSibling("backup.jar")

            restartApplication {
                currentFile.renameTo(backupFile)
                updateFile.renameTo(currentFile)
            }
        }

        /**
         * Restart the current Java application
         * http://lewisleo.blogspot.com/2012/08/programmatically-restart-java.html
         * @param runBeforeRestart some custom code to be run before restarting
         * @throws IOException
         */
        private fun restartApplication(runBeforeRestart: () -> Unit) {
            try {
                // java binary
                val java = System.getProperty("java.home") + "/bin/java"
                // vm arguments
                val vmArguments: List<String> = ManagementFactory.getRuntimeMXBean().inputArguments
                val vmArgsOneLine = StringBuffer()
                for (arg in vmArguments) {
                    // if it's the agent argument : we ignore it otherwise the
                    // address of the old application and the new one will be in conflict
                    if (!arg.contains("-agentlib")) {
                        vmArgsOneLine.append(arg)
                        vmArgsOneLine.append(" ")
                    }
                }
                // init the command to execute, add the vm args
                val cmd = StringBuffer("$java $vmArgsOneLine")
                // program main and program arguments (be careful a sun property. might not be supported by all JVM)
                val mainCommand: Array<String> = System.getProperty("sun.java.command").split(" ").toTypedArray()
                // program main is a jar
                if (mainCommand[0].endsWith(".jar")) {
                    // if it's a jar, add -jar mainJar
                    cmd.append("-jar " + File(mainCommand[0]).path)
                } else {
                    // else it's a .class, add the classpath and mainClass
                    cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0])
                }
                // finally add program arguments
                for (i in 1 until mainCommand.size) {
                    cmd.append(" ")
                    cmd.append(mainCommand[i])
                }
                val cmdString = cmd.toString()
                println(cmdString)

                // execute the command in a shutdown hook, to be sure that all the
                // resources have been disposed before restarting the application
                Runtime.getRuntime().addShutdownHook(thread(start = false) {
                    try {
                        Runtime.getRuntime().exec(cmd.toString())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                })
                // execute some custom code before restarting
                runBeforeRestart()
                // exit
                exitProcess(0)
            } catch (e: Exception) {
                // something went wrong
                throw IOException("Error while trying to restart the application", e)
            }
        }
    }

    private class ProgressResponseBody(
        private val responseBody: ResponseBody,
        private val progressListener: ProgressListener
    ) : ResponseBody() {
        private var bufferedSource: BufferedSource? = null
        override fun contentType(): MediaType? {
            return responseBody.contentType()
        }

        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            if (bufferedSource == null) {
                bufferedSource = source(responseBody.source()).buffer()
            }
            return bufferedSource!!
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
                    return bytesRead
                }
            }
        }
    }

    fun interface ProgressListener {
        fun update(bytesRead: Long, contentLength: Long, done: Boolean)
    }

    class ProgressBarListenerView : View(), ProgressListener {

        private val labelTextProperty = SimpleStringProperty("Loading…")
        private val subLabelTextProperty = SimpleStringProperty("")
        private val progressProperty = SimpleDoubleProperty(ProgressBar.INDETERMINATE_PROGRESS)

        override val root = vbox {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            spacer()
            label(labelTextProperty)
            progressbar(progressProperty) {
                hgrow = Priority.ALWAYS
                fitToParentWidth()
            }
            label(subLabelTextProperty)
            spacer()
        }

        override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
            Platform.runLater {
                if (contentLength < 0) {
                    labelTextProperty.value = "Downloading: ${bytesRead.formatBytes()}"
                    subLabelTextProperty.value = ""
                } else {
                    val progress = bytesRead.toDouble() / contentLength.toDouble()

                    labelTextProperty.value = "Downloading: ${(progress * 100).roundToInt()}%"
                    subLabelTextProperty.value = "${bytesRead.formatBytes()} of ${contentLength.formatBytes()}"
                    progressProperty.value = progress
                }
            }
        }

        private fun Long.formatBytes(): String {
            val absB = if (this == Long.MIN_VALUE) Long.MAX_VALUE else abs(this)
            if (absB < 1024) {
                return "$this B"
            }
            var value = absB
            val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
            var i = 40
            while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
                value = value shr 10
                ci.next()
                i -= 10
            }
            value *= java.lang.Long.signum(this).toLong()
            return String.format("%.1f %ciB", value / 1024.0, ci.current())

        }
    }
}
