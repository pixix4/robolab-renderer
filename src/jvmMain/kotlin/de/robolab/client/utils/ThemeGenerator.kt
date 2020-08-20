package de.robolab.client.utils

import de.robolab.client.theme.Theme
import de.robolab.common.utils.toDashCase
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter

object ThemeGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val targetDirectory = Paths.get("./src/jsClientMain/resources/public/stylesheets/theme/")

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
            appendLine("@import 'theme-utils';")
            appendLine()

            for ((key, value) in getReflectiveProperties(theme.theme.ui)) {
                val line = "\$_$key: $value;"
                appendLine(line)
            }
            appendLine()
            for ((key, value) in getReflectiveProperties(theme.theme.editor)) {
                val line = "\$_$key: $value;"
                appendLine(line)
            }

            appendLine()

            appendLine(if (isDefault) {
                "body {"
            } else {
                "body[data-theme='${theme.name.toDashCase()}'] {"
            })
            appendLine("    @include load-color-vars();")
            appendLine("}")

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
                appendLine("@use '_${theme.name.toDashCase()}';")
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
            appendLine("@mixin load-color-vars {")
            appendLine()

            for ((key, _) in getReflectiveProperties(Theme.DEFAULT.theme.ui)) {
                val line = "    --$key: #{\$_$key};"
                appendLine(line)
            }
            appendLine()
            for ((key, _) in getReflectiveProperties(Theme.DEFAULT.theme.editor)) {
                val line = "    --$key: #{\$_$key};"
                appendLine(line)
            }

            appendLine()

            appendLine("}")

        }

        val path = targetDirectory.resolve(fileName)
        Files.writeString(path, content)
    }

    private fun getReflectiveProperties(obj: Any): Map<String, String> {
        val content = mutableMapOf<String, String>()
        for (prop in obj::class.declaredMemberProperties) {
            val value = prop.javaGetter?.invoke(obj) ?: Unit
            content += prop.name.toDashCase() to value.toString()
        }
        return content
    }

    private fun StringBuilder.appendDisclaimer() {
        appendLine("/**********************************************************")
        appendLine(" *                                                        *")
        appendLine(" *           Auto generated file. DO NOT EDIT!            *")
        appendLine(" * See: 'src/commonMain/kotlin/de/robolab/renderer/theme' *")
        appendLine(" *        Generate with './gradlew buildSassTheme'        *")
        appendLine(" *                                                        *")
        appendLine(" **********************************************************/")
        appendLine()
    }
}
