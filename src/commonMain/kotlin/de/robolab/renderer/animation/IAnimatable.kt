package de.robolab.renderer.animation

import de.westermann.kobserve.event.EventHandler

interface IAnimatable {
    
    fun onUpdate(msOffset: Double): Boolean
    
    val isRunning: Boolean
    
    val onAnimationStart: EventHandler<Unit>
    val onAnimationFinish: EventHandler<Unit>
}