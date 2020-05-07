package de.robolab.repository

import de.westermann.kobserve.list.observableListOf

class MemoryRepository<T : IEntry> : IRepository<T> {

    override val content = observableListOf<T>()

    override fun add(entry: T) {
        content += entry
    }

    override fun update(entry: T) {
        // content.
    }

    override fun remove(entry: T) {
        content -= entry
    }
}