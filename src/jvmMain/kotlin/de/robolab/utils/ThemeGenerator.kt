package de.robolab.utils

import de.robolab.theme.Theme
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter

object ThemeGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val targetDirectory = Paths.get("./src/jsMain/resources/public/stylesheets/theme/")

        generateThemeUtilsFile(targetDirectory)

        for (theme in Theme.values()) {
            generateThemeFile(theme, theme == Theme.DEFAULT, targetDirectory)
        }

        generateExportsFile(targetDirectory)
    }

    private fun generateThemeFile(theme: Theme, isDefault: Boolean, targetDirectory: Path) {
        val fileName = "_${theme.name.toDashCase()}.scss"
        println("Generate file $fileName")

        val content = buildString {
            appendDisclaimer()
            appendln("@import 'theme-utils';")
            appendln()

            for ((key, value) in getReflectiveProperties(theme.theme.ui)) {
                val line = "\$_$key: $value;"
                appendln(line)
            }
            appendln()
            for ((key, value) in getReflectiveProperties(theme.theme.editor)) {
                val line = "\$_$key: $value;"
                appendln(line)
            }

            appendln()

            appendln(if (isDefault) {
                "body {"
            } else {
                "body[data-theme='${theme.name.toDashCase()}'] {"
            })
            appendln("    @include load-color-vars();")
            appendln("}")

        }

        val path = targetDirectory.resolve(fileName)
        Files.writeString(path, content)
    }

    private fun generateExportsFile(targetDirectory: Path) {
        val fileName = "_theme-exports.scss"
        println("Generate file $fileName")

        val content = buildString {
            appendDisclaimer()
            for (theme in Theme.values().sortedBy { it.name }) {
                appendln("@use '_${theme.name.toDashCase()}';")
            }
        }

        val path = targetDirectory.resolve(fileName)
        Files.writeString(path, content)
    }

    private fun generateThemeUtilsFile(targetDirectory: Path) {
        val fileName = "_theme-utils.scss"
        println("Generate file $fileName")


        val content = buildString {
            appendDisclaimer()
            appendln("@mixin load-color-vars {")
            appendln()

            for ((key, _) in getReflectiveProperties(Theme.DEFAULT.theme.ui)) {
                val line = "    --$key: #{\$_$key};"
                appendln(line)
            }
            appendln()
            for ((key, _) in getReflectiveProperties(Theme.DEFAULT.theme.editor)) {
                val line = "    --$key: #{\$_$key};"
                appendln(line)
            }

            appendln()

            appendln("}")

        }

        val path = targetDirectory.resolve(fileName)
        Files.writeString(path, content)
    }

    private fun getReflectiveProperties(obj: Any): Map<String, String> {
        val content = mutableMapOf<String, String>()
        for (prop in obj::class.declaredMemberProperties) {
            val value = prop.javaGetter?.invoke(obj) ?: Unit;
            content += prop.name.toDashCase() to value.toString()
        }
        return content
    }

    private fun StringBuilder.appendDisclaimer() {
        appendln("/**********************************************************")
        appendln(" *                                                        *")
        appendln(" *           Auto generated file. DO NOT EDIT!            *")
        appendln(" * See: 'src/commonMain/kotlin/de/robolab/renderer/theme' *")
        appendln(" *        Generate with './gradlew buildSassTheme'        *")
        appendln(" *                                                        *")
        appendln(" **********************************************************/")
        appendln()
    }
}
