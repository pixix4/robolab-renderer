package de.robolab.client.app.controller

import de.robolab.common.utils.toFixed
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.log10
import kotlin.math.pow

class ProgressController {

    val progressList = observableListOf<Entry>()

    fun <T : Number> startPercentProgress(
        name: String,
        current: T,
        total: T,
        format: (current: T, total: T) -> String = { c, t ->
            (c.toDouble() / t.toDouble()).toInt().toString() + "%"
        }
    ): PercentEntry<T> {
        val entry = PercentEntry(name, current, total, format)

        progressList += entry

        return entry
    }

    fun startIndeterminateProgress(
        name: String,
        label: String
    ): IndeterminateEntry {
        val entry = IndeterminateEntry(name, label)

        progressList += entry

        return entry
    }

    abstract inner class Entry(
        val name: String
    ) {

        abstract val labelProperty: ObservableValue<String>

        fun finish() {
            progressList -= this
        }
    }

    inner class PercentEntry<T : Number> internal constructor(
        name: String,
        current: T,
        val total: T,
        val format: (current: T, total: T) -> String
    ) : Entry(name) {

        private val currentProperty = property(current)

        val percentProperty = currentProperty.mapBinding {
            it.toDouble() / total.toDouble()
        }
        override val labelProperty = currentProperty.mapBinding {
            format(it, total)
        }

        fun update(current: T) {
            currentProperty.value = current
        }
    }

    inner class IndeterminateEntry internal constructor(
        name: String,
        label: String
    ) : Entry(name) {

        override val labelProperty = property(label)

        fun update(label: String) {
            labelProperty.value = label
        }
    }

    fun readableFileSize(size: Long): String {
        if (size <= 1) return "$size byte"
        val units = arrayOf("bytes", "KB", "MB", "GB", "TB", "PB", "EB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return (size / 1024.0.pow(digitGroups)).toFixed(2) + " " + units[digitGroups]
    }
}
