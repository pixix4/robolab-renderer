package de.robolab.client.renderer.canvas

import de.robolab.client.renderer.events.*
import de.robolab.common.utils.Dimension

@Suppress("SuspiciousCollectionReassignment")
class CanvasListenerManager: ICanvasListener {

    private var listenerList = emptyList<ICanvasListener>()
    fun addListener(listener: ICanvasListener) {
        listenerList += listener
    }
    operator fun plusAssign(listener: ICanvasListener) = addListener(listener)

    fun removeListener(listener: ICanvasListener) {
        listenerList -= listener
    }
    operator fun minusAssign(listener: ICanvasListener) = removeListener(listener)

    override fun onPointerDown(event: PointerEvent) {
        for (listener in listenerList) {
            listener.onPointerDown(event)
        }
    }

    override fun onPointerUp(event: PointerEvent) {
        for (listener in listenerList) {
            listener.onPointerUp(event)
        }
    }

    override fun onPointerMove(event: PointerEvent) {
        for (listener in listenerList) {
            listener.onPointerMove(event)
        }
    }

    override fun onPointerDrag(event: PointerEvent) {
        for (listener in listenerList) {
            listener.onPointerDrag(event)
        }
    }

    override fun onPointerSecondaryAction(event: PointerEvent) {
        for (listener in listenerList) {
            listener.onPointerSecondaryAction(event)
        }
    }

    override fun onPointerEnter(event: PointerEvent) {
        for (listener in listenerList) {
            listener.onPointerEnter(event)
        }
    }

    override fun onPointerLeave(event: PointerEvent) {
        for (listener in listenerList) {
            listener.onPointerLeave(event)
        }
    }

    override fun onScroll(event: ScrollEvent) {
        for (listener in listenerList) {
            listener.onScroll(event)
        }
    }

    override fun onZoom(event: ZoomEvent) {
        for (listener in listenerList) {
            listener.onZoom(event)
        }
    }

    override fun onRotate(event: RotateEvent) {
        for (listener in listenerList) {
            listener.onRotate(event)
        }
    }

    override fun onResize(size: Dimension) {
        for (listener in listenerList) {
            listener.onResize(size)
        }
    }

    override fun onKeyPress(event: KeyEvent) {
        for (listener in listenerList) {
            listener.onKeyPress(event)
        }
    }

    override fun onKeyRelease(event: KeyEvent) {
        for (listener in listenerList) {
            listener.onKeyRelease(event)
        }
    }
}