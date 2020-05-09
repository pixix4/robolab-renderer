package de.robolab.theme

import de.robolab.renderer.data.Color

class ParsedITermColors: ITermColors {

     private val colorMap = parseITermFormat(TODO())

    override val backgroundColor: Color = colorMap["background color"] ?: Color.WHITE
    override val foregroundColor: Color = colorMap["foreground color"] ?: Color.WHITE

    override val blackColor: Color = colorMap["ansi 0 color"] ?: Color.WHITE
    override val redColor: Color = colorMap["ansi 1 color"] ?: Color.WHITE
    override val greenColor: Color = colorMap["ansi 2 color"] ?: Color.WHITE
    override val yellowColor: Color = colorMap["ansi 3 color"] ?: Color.WHITE
    override val blueColor: Color = colorMap["ansi 4 color"] ?: Color.WHITE
    override val magentaColor: Color = colorMap["ansi 5 color"] ?: Color.WHITE
    override val cyanColor: Color = colorMap["ansi 6 color"] ?: Color.WHITE
    override val whiteColor: Color = colorMap["ansi 7 color"] ?: Color.WHITE

    override val blackBrightColor: Color = colorMap["ansi 8 color"] ?: Color.WHITE
    override val redBrightColor: Color = colorMap["ansi 9 color"] ?: Color.WHITE
    override val greenBrightColor: Color = colorMap["ansi 10 color"] ?: Color.WHITE
    override val yellowBrightColor: Color = colorMap["ansi 11 color"] ?: Color.WHITE
    override val blueBrightColor: Color = colorMap["ansi 12 color"] ?: Color.WHITE
    override val magentaBrightColor: Color = colorMap["ansi 13 color"] ?: Color.WHITE
    override val cyanBrightColor: Color = colorMap["ansi 14 color"] ?: Color.WHITE
    override val whiteBrightColor: Color = colorMap["ansi 15 color"] ?: Color.WHITE

    companion object {

        private fun parseITermFormat(content: String): Map<String, Color> {
            val outerRegex = """<key>([a-zA-Z0-9 ]*)</key>\s*<dict>((?:.*?\s)*?)</dict>""".toRegex()
            val innerRegex = """<key>([a-zA-Z0-9 ]*)</key>\s*<[a-z]+>(.*)</[a-z]+>""".toRegex()

            val map = outerRegex.findAll(content.toLowerCase()).map { outerResult ->
                val colorKey = outerResult.groupValues[1]
                val innerDict = outerResult.groupValues[2]

                val innerMap = innerRegex.findAll(innerDict).map { innerResult ->
                    val key = innerResult.groupValues[1]
                    val value = innerResult.groupValues[2]
                    key to value
                }.toMap()

                val r = innerMap["red component"]?.toDoubleOrNull() ?: 1.0
                val g = innerMap["green component"]?.toDoubleOrNull() ?: 1.0
                val b = innerMap["blue component"]?.toDoubleOrNull() ?: 1.0
                val a = innerMap["alpha component"]?.toDoubleOrNull() ?: 1.0

                colorKey to Color(
                        (r * 255).toInt(),
                        (g * 255).toInt(),
                        (b * 255).toInt(),
                        a
                )
            }.toMap()

            println(map)

            return map
        }
    }
}