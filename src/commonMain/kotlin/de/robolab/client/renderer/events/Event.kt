package de.robolab.client.renderer.events

abstract class Event(
        var bubbles: Boolean = true
) {
    
    fun stopPropagation() {
        bubbles = false
    }
}