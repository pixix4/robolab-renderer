package de.robolab.utils

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz

class Logger(val name: String) {

    val level by PreferenceStorage.logLevelProperty

    val output = LoggerOutput()

    fun log(level: Level, msg: () -> Any?) {
        if (level.index <= this.level.index) {
            output.log(this, level, msg())
        }
    }

    fun log(level: Level, msg: Any?) {
        if (level.index <= this.level.index) {
            output.log(this, level, msg)
        }
    }

    fun getCurrentDate(): String {
        return LOG_DATE_FORMAT.format(DateTimeTz.nowLocal())
    }

    fun error(msg: () -> Any?) = log(Level.ERROR, msg)
    fun error(msg: Any?) = log(Level.ERROR, msg)
    fun e(msg: () -> Any?) = log(Level.ERROR, msg)
    fun e(msg: Any?) = log(Level.ERROR, msg)

    fun warn(msg: () -> Any?) = log(Level.WARN, msg)
    fun warn(msg: Any?) = log(Level.WARN, msg)
    fun w(msg: () -> Any?) = log(Level.WARN, msg)
    fun w(msg: Any?) = log(Level.WARN, msg)

    fun info(msg: () -> Any?) = log(Level.INFO, msg)
    fun info(msg: Any?) = log(Level.INFO, msg)
    fun i(msg: () -> Any?) = log(Level.INFO, msg)
    fun i(msg: Any?) = log(Level.INFO, msg)

    fun debug(msg: () -> Any?) = log(Level.DEBUG, msg)
    fun debug(msg: Any?) = log(Level.DEBUG, msg)
    fun d(msg: () -> Any?) = log(Level.DEBUG, msg)
    fun d(msg: Any?) = log(Level.DEBUG, msg)

    @Suppress("NOTHING_TO_INLINE")
    inline fun printStacktrace() {
        output.printStacktrace()
    }

    enum class Level(val index: Int) {
        ERROR(1), WARN(2), INFO(3), DEBUG(4)
    }

    companion object {
        val DEFAULT = Logger("DefaultLogger")

        private val loggerCache = mutableMapOf<String, Logger>()
        operator fun invoke(thisRef: Any?): Logger {
            val ref = thisRef ?: return DEFAULT
            val name = ref::class.simpleName ?: return DEFAULT
            return loggerCache.getOrPut(name) { Logger(name) }
        }

        val LOG_DATE_FORMAT = DateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    }
}

expect class LoggerOutput() {
    fun log(logger: Logger, level: Logger.Level, msg: Any?)

    fun printStacktrace()
}
