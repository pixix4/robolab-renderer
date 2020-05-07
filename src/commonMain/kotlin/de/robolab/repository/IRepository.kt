package de.robolab.repository

import de.westermann.kobserve.base.ObservableList

interface IRepository<T: IEntry> {
    
    val content: ObservableList<T>
    
    fun add(entry: T)
    
    fun update(entry: T)
    
    fun remove(entry: T)
}
