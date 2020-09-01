package de.robolab.client.updater

import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import tornadofx.*
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs
import kotlin.math.roundToInt

class UpdateBarView : View(), Downloader.ProgressListener {

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

            print("\r${labelTextProperty.value} ${subLabelTextProperty.value}             ")
            if (done) {
                println()
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
