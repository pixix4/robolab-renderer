package de.robolab.renderer.platform

abstract class Event(
        var bubbles: Boolean = true
) {
    
    fun stopPropagation() {
        bubbles = false
    }
}