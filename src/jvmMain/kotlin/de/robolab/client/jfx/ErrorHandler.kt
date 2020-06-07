package de.robolab.client.jfx

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.client.jfx.utils.iconNoAdd
import de.robolab.common.utils.Logger
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.layout.VBox
import tornadofx.FX
import tornadofx.label
import tornadofx.textarea
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

        Alert(Alert.AlertType.ERROR).apply {
            title = error.message ?: "An error occurred"
            isResizable = true
            headerText = if (error.stackTrace.isNullOrEmpty()) "Error" else "Error in " + error.stackTrace[0].toString()

            dialogPane.content = VBox().apply {
                label(error.cause?.message ?: "") {
                    style = "-fx-font-weight: bold"
                }

                val writer = StringWriter()
                error.printStackTrace(PrintWriter(writer))
                textarea(writer.toString()) {
                    prefRowCount = 20
                    prefColumnCount = 50
                    isEditable = false
                }
            }
            graphic = iconNoAdd(MaterialIcon.ERROR_OUTLINE, "3em")

            FX.applyStylesheetsTo(dialogPane.scene)

            showAndWait()
        }
    }
}
