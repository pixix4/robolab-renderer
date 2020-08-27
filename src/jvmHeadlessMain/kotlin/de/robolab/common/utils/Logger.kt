package de.robolab.common.utils

actual class LoggerOutput actual constructor() {
    actual fun log(logger: Logger, level: Logger.Level, msg: Any?) {
        val color = when (level) {
            Logger.Level.ERROR -> "\u001B[31m"
            Logger.Level.WARN -> "\u001B[33m"
            Logger.Level.INFO -> "\u001B[34m"
            Logger.Level.DEBUG -> "\u001B[32m"
        }
        println(
                "${logger.getCurrentDate()} [$color${level.name.padEnd(5, ' ')}\u001B[0m] ${logger.name}: $msg"
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun printStacktrace() {
        println(Thread.currentThread().stackTrace.toList().drop(1).joinToString("\n    "))
    }
}
