package de.robolab.client.renderer.view.base

import de.robolab.client.renderer.transition.IAnimatable
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.event.once

@Suppress("SuspiciousCollectionReassignment")
class AnimatableManager {

    val onAnimationStart = EventHandler<Unit>()
    val onAnimationFinish = EventHandler<Unit>()

    var enabled = false
    set(value) {
        field = value
        for (animation in animatableMap.keys) {
            animation.enabled = enabled
        }
    }

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
        element.enabled = enabled
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
        if (animatableMap.keys.any { it.isRunning }) {
            onAnimationFinish.once(callback)
        } else {
            callback(Unit)
        }
    }

    fun onFinish(callback: () -> Unit) {
        onFinish { _: Unit -> callback() }
    }
}