package de.westermann.kobserve.event

import de.westermann.kobserve.base.ObservableValue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This class represents a simple event handler who manages listeners for an event of type 'E'.
 */
@Suppress("SuspiciousCollectionReassignment")
class SuspendEventHandler<E>() {

    private var listeners: Map<suspend (E) -> Unit, SuspendEventListener<E>?> = emptyMap()

    /**
     * Add an event listener to this handler if it is not already present.
     *
     * @param listener The event listener to attach.
     *
     * @return The event listener that was added or null if it was already present.
     */
    fun addListener(listener: suspend (E) -> Unit): suspend (E) -> Unit {
        if (listener !in listeners) {
            listeners += listener to null
            onAttach()
        }

        return listener
    }

    /**
     * Remove an event listener from this handler.
     *
     * @param listener The event listener to detach.
     */
    fun removeListener(listener: suspend (E) -> Unit) {
        if (listener in listeners) {
            listeners -= listener
            onDetach()
        }
    }

    /**
     * Remove all event listeners from this handler.
     */
    fun clearListeners() {
        if (listeners.isNotEmpty()) {
            listeners = emptyMap()
            onDetach()
        }
    }

    /**
     * Emit an event to all assigned event listeners.
     *
     * @param event The event to emit.
     */
    suspend fun emit(event: E) {
        for (listener in listeners.keys) {
            listener(event)
        }
    }

    /**
     * @see addListener
     */
    operator fun invoke(listener: suspend (E) -> Unit) {
        addListener(listener)
    }

    /**
     * @see addListener
     */
    operator fun plusAssign(listener: suspend (E) -> Unit) {
        addListener(listener)
    }

    /**
     * @see removeListener
     */
    operator fun minusAssign(listener: suspend (E) -> Unit) {
        removeListener(listener)
    }

    /**
     * Add an event listener to this handler if it is not already present.
     *
     * @param listener The event listener to attach.
     *
     * @return A reference object to the added listener or null if it was already present.
     */
    fun reference(listener: suspend (E) -> Unit): SuspendEventListener<E> {
        addListener(listener)

        var reference = listeners[listener]
        if (reference == null) {
            reference = Listener(listener)
            listeners += listener to reference
        }

        return reference
    }

    /**
     * Returns the count of assigned event listeners.
     */
    val size: Int
        get() = listeners.size

    operator fun contains(element: suspend (E) -> Unit): Boolean {
        return listeners.contains(element)
    }

    fun isEmpty(): Boolean {
        return listeners.isEmpty()
    }

    operator fun iterator(): Iterator<suspend (E) -> Unit> {
        return listeners.keys.iterator()
    }

    var onAttach: () -> Unit = {}
    var onDetach: () -> Unit = {}

    constructor(vararg dependencies: SuspendEventHandler<out E>) : this() {
        dependencies.forEach { eventHandler ->
            eventHandler.addListener { event ->
                emit(event)
            }
        }
    }

    private inner class Listener(
            private val listener: suspend (E) -> Unit
    ) : SuspendEventListener<E> {

        override suspend fun emit(event: E) {
            listener(event)
        }

        /**
         * Checks if the referencing event listener is part of the parent event handler.
         */
        override val isAttached: Boolean
            get() = listener in listeners

        /**
         * Add the referencing event listener to parent event handler if it is not already present.
         *
         * @return True if the referencing event listener was added.
         */
        override fun attach(): Boolean {
            if (isAttached) {
                return false
            }

            addListener(listener)
            listeners += listener to this
            return true
        }

        /**
         * Remove the referencing event listener to parent event handler if it is present.
         *
         * @return True if the referencing event listener was removed.
         */
        override fun detach(): Boolean {
            if (!isAttached) {
                return false
            }

            removeListener(listener)
            return true
        }
    }
}

/**
 * Utility function that allows simple event binding of an unit event to another generic event.
 *
 * @param handler An generic event handler to listen to.
 * @receiver The unit event handler that should listen.
 */
fun SuspendEventHandler<Unit>.listenTo(handler: SuspendEventHandler<*>) {
    handler {
        emit()
    }
}

/**
 * Combine two common event handler to listen two both simultaneously.
 */
infix fun <T> SuspendEventHandler<out T>.and(other: SuspendEventHandler<out T>): SuspendEventHandler<T> =
        SuspendEventHandler(this, other)

/**
 * Combine two common event handler to listen two both simultaneously.
 */
fun <T> SuspendEventHandler<out T>.and(other: SuspendEventHandler<out T>, listener: suspend (T) -> Unit): SuspendEventHandler<T> =
        SuspendEventHandler(this, other).also { it += listener }

/**
 * Combine two common event handler to listen two both simultaneously.
 */
operator fun <T> SuspendEventHandler<out T>.plus(other: SuspendEventHandler<out T>): SuspendEventHandler<T> =
        SuspendEventHandler(this, other)

@Suppress("NOTHING_TO_INLINE")
suspend inline fun SuspendEventHandler<Unit>.emit() {
    emit(Unit)
}

fun <T> SuspendEventHandler<T>.once(listener: suspend (T) -> Unit) {
    var temp: suspend (T) -> Unit = {}
    temp = addListener {
        listener(it)
        removeListener(temp)
    }
}

suspend fun <T> SuspendEventHandler<T>.next(): T {
    return suspendCoroutine { continuation ->
        once {
            continuation.resume(it)
        }
    }
}

suspend fun <T> SuspendEventHandler<T>.now(value: T, listener: suspend (T) -> Unit) {
    addListener(listener)
    listener(value)
}

suspend fun SuspendEventHandler<Unit>.now(listener: suspend (Unit) -> Unit) {
    now(Unit, listener)
}

fun <T, E> ObservableValue<T>.mapEvent(transform: (T) -> SuspendEventHandler<E>): SuspendEventHandler<E> {
    val handler = SuspendEventHandler<E>()

    var reference: SuspendEventListener<E>? = null

    fun update() {
        reference?.detach()
        reference = transform(value).reference {
            handler.emit(it)
        }
    }
    update()

    return handler
}
