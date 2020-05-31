package de.robolab.client.jfx

import de.robolab.common.utils.Logger
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.scene.text.FontSmoothingType
import tornadofx.add
import tornadofx.style
import java.io.PrintWriter
import java.io.StringWriter

class ErrorHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, error: Throwable) {
        val log = Logger(t.name)

        val writer = StringWriter()
        error.printStackTrace(PrintWriter(writer))
        val message = writer.toString()

        if (isSilent(error)) {
            log.warn("Silent uncaught exception. This error is known and comes from the underlying javafx system where it cannot be fixed.\nIf you think this is a false positive please report this error message: $message")
            return
        }

        log.error(message)

        if (isCycle(error)) {
            log.info("Detected cycle handling error, aborting.")
        } else {
            Platform.runLater {
                showErrorDialog(error)
            }
        }
    }

    private fun isSilent(error: Throwable): Boolean {
        // Wayland drag n drop error
        return error.stackTrace.any {
            it.className == "javafx.scene.Scene\$DropTargetListener" && it.methodName == "drop"
        }
    }

    private fun isCycle(error: Throwable) = error.stackTrace.any {
        it.className.startsWith("${javaClass.name}\$uncaughtException$")
    }

    private fun showErrorDialog(error: Throwable) {
        val cause = Label(if (error.cause != null) error.cause?.message else "").apply {
            style = "-fx-font-weight: bold"
        }

        val textarea = TextArea().apply {
            prefRowCount = 20
            prefColumnCount = 50

            val writer = StringWriter()
            error.printStackTrace(PrintWriter(writer))
            text = writer.toString()

            style {
                fontSmoothingType = FontSmoothingType.LCD
            }
        }

        Alert(Alert.AlertType.ERROR).apply {
            title = error.message ?: "An error occured"
            isResizable = true
            headerText = if (error.stackTrace.isNullOrEmpty()) "Error" else "Error in " + error.stackTrace[0].toString()
            dialogPane.content = VBox().apply {
                add(cause)
                add(textarea)
            }

            showAndWait()
        }
    }
}
