package de.robolab.common.utils

import kotlinx.datetime.Clock

class Logger(val name: String) {

    fun log(level: Level, msg: () -> Any?) {
        if (level.index <= Logger.level.index) {
            val m = format(msg())
            output.log(this, level, m)
        }
    }

    fun log(level: Level, vararg msg: Any?) {
        if (level.index <= Logger.level.index) {
            val m = msg.map {
                format(it)
            }.toTypedArray()
            output.log(this, level, *m)
        }
    }

    fun getCurrentDate(): String {
        return formatDateTime(Clock.System.now(), LOG_DATE_FORMAT)
    }

    fun error(msg: () -> Any?) = log(Level.ERROR, msg)
    fun error(vararg msg: Any?) = log(Level.ERROR, msg)

    fun warn(msg: () -> Any?) = log(Level.WARN, msg)
    fun warn(vararg msg: Any?) = log(Level.WARN, msg)

    fun info(msg: () -> Any?) = log(Level.INFO, msg)
    fun info(vararg msg: Any?) = log(Level.INFO, msg)

    fun debug(msg: () -> Any?) = log(Level.DEBUG, msg)
    fun debug(vararg msg: Any?) = log(Level.DEBUG, msg)

    @Suppress("NOTHING_TO_INLINE")
    inline fun printStacktrace() {
        output.printStacktrace()
    }

    enum class Level(val index: Int) {
        ERROR(1), WARN(2), INFO(3), DEBUG(4);
    }

    companion object {
        val DEFAULT = Logger("DefaultLogger")

        val output = LoggerOutput()

        var level = Level.INFO

        private val loggerCache = mutableMapOf<String, Logger>()
        operator fun invoke(thisRef: Any?): Logger {
            val ref = thisRef ?: return DEFAULT
            val name = ref::class.simpleName ?: return DEFAULT
            return loggerCache.getOrPut(name) { Logger(name) }
        }

        fun format(msg: Any?): Any? {
            if (msg is Throwable) {
                return msg.stackTraceToString()
            }
            return msg
        }

        const val LOG_DATE_FORMAT = "YYYY-MM-DD HH:mm:ss.SSS"
    }
}

expect class LoggerOutput() {
    fun log(logger: Logger, level: Logger.Level, vararg msg: Any?)

    fun printStacktrace()
}

val Any.autoLogger:Logger
    get() = Logger(this)
