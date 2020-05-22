package de.robolab.common.utils

actual class LoggerOutput actual constructor() {
    actual fun log(logger: Logger, level: Logger.Level, msg: Any?) {
        val color = when (level) {
            Logger.Level.ERROR -> "#c0392b"
            Logger.Level.WARN -> "#f39c12"
            Logger.Level.INFO -> "#2980b9"
            Logger.Level.DEBUG -> "#2ecc71"
        }
        console.log(
                "%c${logger.getCurrentDate()} [%c${level.name.padEnd(5, ' ')}%c] ${logger.name}:",
                "color: initial",
                "color: $color",
                "color: initial",
                msg
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun printStacktrace() {
        js("console.trace()")
    }
}
