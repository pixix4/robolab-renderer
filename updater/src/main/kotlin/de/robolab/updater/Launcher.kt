package de.robolab.updater

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.system.exitProcess

class Main: CliktCommand() {

    private val sourceUrl by option("--url", help = "Read update from URL")
        .required()

    private val targetFile by option("--file", help = "Write update to FILE")
        .required()

    override fun run() {
        MainApp.main(arrayOf(
            sourceUrl,
            targetFile
        ))
    }
}

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) = Main().main(args)
}
