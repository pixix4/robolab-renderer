package de.robolab.repository

interface IEntry{

    val key: Any
    val repository: IRepository<IEntry>
    
    fun update() {
        repository.update(this)
    }
    
    fun delete() {
        repository.remove(this)
    }
}