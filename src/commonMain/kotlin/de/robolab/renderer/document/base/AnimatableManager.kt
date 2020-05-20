package de.robolab.renderer.document.base

import de.robolab.renderer.animation.IAnimatable
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.event.once

@Suppress("SuspiciousCollectionReassignment")
class AnimatableManager {

    val onAnimationStart = EventHandler<Unit>()
    val onAnimationFinish = EventHandler<Unit>()

    private var forceRedraw = false
    private var internalIsRunning = false
    val isRunning: Boolean
        get() = internalIsRunning

    private var animatableMap: Map<IAnimatable, List<EventListener<*>>> = emptyMap()

    var updateList = emptyList<IAnimatable>()

    private fun checkEvents() {
        updateList = animatableMap.keys.filter { it.isRunning }
        val shouldRun = updateList.isNotEmpty() || forceRedraw

        if (internalIsRunning == shouldRun) return

        internalIsRunning = shouldRun
        if (shouldRun) {
            onAnimationStart.emit()
        } else {
            onAnimationFinish.emit()
        }
    }

    fun registerAnimatable(element: IAnimatable) {
        animatableMap += element to listOf(
                element.onAnimationStart.reference {
                    checkEvents()
                },
                element.onAnimationFinish.reference {
                    checkEvents()
                }
        )

        if (element.isRunning) {
            checkEvents()
        }
    }

    fun unregisterAnimatable(element: IAnimatable) {
        animatableMap[element]?.forEach { it.detach() }
        animatableMap -= element
        checkEvents()
    }

    fun onUpdate(msOffset: Double): Boolean {
        val result = isRunning

        if (forceRedraw) {
            forceRedraw = false
            checkEvents()
        }

        for (animatable in updateList) {
            animatable.onUpdate(msOffset)
        }

        return result
    }

    fun requestRedraw() {
        forceRedraw = true
        checkEvents()
    }
    
    fun onFinish(callback: (Unit) -> Unit) {
        if (!isRunning) {
            callback(Unit)
        } else {
            onAnimationFinish.once(callback)
        }
    }
    fun onFinish(callback: () -> Unit) {
        onFinish { _: Unit -> callback() }
    }
}