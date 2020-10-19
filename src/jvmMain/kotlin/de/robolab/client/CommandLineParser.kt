package de.robolab.client

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.theme.LightTheme
import de.robolab.client.ui.MainApp
import de.robolab.client.ui.adapter.AwtCanvas
import de.robolab.client.updater.UpdaterApp
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Rectangle
import java.io.File

class CommandLineParser: CliktCommand(invokeWithoutSubcommand = true) {

    private val layout by option("--layout", help = "Layout of the plotter (row x col)")
    private val groups by option("--groups", help = "Init loaded groups (Separated with '+')")
    private val connect by option("--connect").flag(default = false)
    private val fullscreen by option("--fullscreen").flag(default = false)

    override fun run() {
        val subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            MainApp.main(arrayOf(
                (layout ?: "").toString(),
                (groups ?: "").toString(),
                connect.toString(),
                fullscreen.toString()
            ))
        }
    }

    init {
        versionOption(BuildInformation.versionClient.toString())
    }

    class Update: CliktCommand() {

        private val sourceUrl by option("--url", help = "Read update from URL")
            .required()

        private val targetFile by option("--file", help = "Write update to FILE")
            .required()

        override fun run() {
            UpdaterApp.main(
                arrayOf(
                    sourceUrl,
                    targetFile
                )
            )
        }
    }

    class Export: CliktCommand() {

        private val scale by option("--scale")
            .double()
            .default(4.0)

        private val recursive by option("--recursive", "-r")
            .flag(default = false)

        private val progress by option("--progress", "-p")
            .flag(default = false)

        enum class Format {
            PNG, SVG
        }

        private val format by option("--format", "-f")
            .enum<Format>(ignoreCase = true)
            .multiple(listOf(Format.PNG))

        private val inputFilePath by argument("INPUT")
            .file(mustBeReadable = true)

        private val outputFilePath by argument("OUTPUT")
            .file()

        private fun export(inputFile: File, outputFile: File, type: Format, log: (String) -> Unit) {
            val ending = type.name.toLowerCase()
            try {
                if (!outputFile.exists()) {
                    if (!outputFile.name.endsWith(ending, true)) {
                        outputFile.mkdirs()
                    } else {
                        val parent = outputFile.parentFile
                        if (!parent.exists()) {
                            parent.mkdirs()
                        }
                    }
                }
                val out = if (outputFile.isDirectory) {
                    outputFile.resolve(inputFile.nameWithoutExtension + ".$ending")
                } else outputFile

                log("Export ${inputFile.absolutePath} to ${out.absolutePath}")

                val planetFile = PlanetFile(inputFile.readText())

                val rect = AbsPlanetDrawable.calcPlanetArea(planetFile.planet)?.expand(0.99) ?: Rectangle.ZERO
                val exportSize =
                    Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)

                when (type) {
                    Format.PNG -> {
                        val canvas = AwtCanvas(exportSize, scale)
                        exportToCanvas(planetFile, canvas)
                        canvas.writePNG(out)
                    }
                    Format.SVG -> {
                        val canvas = SvgCanvas(exportSize)
                        exportToCanvas(planetFile, canvas)
                        out.writeText(canvas.buildFile())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun exportToCanvas(
            planetFile: PlanetFile,
            canvas: ICanvas
        ) {
            val drawable = SimplePlanetDrawable()
            drawable.drawCompass = false
            drawable.drawName = true
            drawable.importPlanet(planetFile.planet)

            val planetDocument = HeadlessPlanetDocument(drawable.view)
            val plotter = PlotterWindow(canvas, planetDocument, LightTheme, 0.0)

            drawable.centerPlanet()

            plotter.render(0.0)
        }

        private val exportPlanList = mutableListOf<Pair<File, File>>()

        private fun exportDirectory(inputDirectory: File, outputDirectory: File) {
            try {
                for (file in inputDirectory.listFiles() ?: return) {
                    if (file.isDirectory && recursive) {
                        exportDirectory(file, outputDirectory.resolve(file.name))
                    } else if (file.isFile && file.extension.endsWith("planet", true)) {
                        exportPlanList += file to outputDirectory
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun run() {
            if (inputFilePath.isDirectory) {
                if (outputFilePath.isDirectory) {
                    exportDirectory(inputFilePath, outputFilePath)
                } else {
                    throw IllegalArgumentException("$outputFilePath must be a directory!")
                }
            } else {
                exportPlanList += inputFilePath to outputFilePath
            }

            var index = 0
            for ((input, output) in exportPlanList) {
                index += 1

                val progressLine = "Progress: $index of ${exportPlanList.size} (${index * 100 / exportPlanList.size}%)       \r"

                if (progress) {
                    print(progressLine)
                }

                for (type in format) {
                    export(input, output, type) {
                        println(it)
                        if (progress) {
                            print(progressLine)
                        }
                    }
                }
            }
        }
    }
}