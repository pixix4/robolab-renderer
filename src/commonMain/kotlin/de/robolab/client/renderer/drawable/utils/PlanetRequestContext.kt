package de.robolab.client.renderer.drawable.utils

import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.PlanetPoint
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlanetRequestContext {

    enum class Type {
        POINT,
        POINT_END,
        PATH,
    }

    private var requestType: Type? = null
    private var cont: CancellableContinuation<*>? = null

    val type: Type?
        get() = requestType

    fun providePoint(point: PlanetPoint) {
        val type = requestType ?: return
        if (type != Type.POINT) return

        @Suppress("UNCHECKED_CAST") val cont =
            (cont ?: return) as CancellableContinuation<PlanetPoint?>

        cont.resume(point)

        this.requestType = null
        this.cont = null
    }

    fun providePointEnd(point: PlanetPoint, direction: PlanetDirection) {
        val type = requestType ?: return
        if (type != Type.POINT_END) return

        @Suppress("UNCHECKED_CAST") val cont =
            (cont ?: return) as CancellableContinuation<Pair<PlanetPoint, PlanetDirection>?>

        cont.resume(point to direction)

        this.requestType = null
        this.cont = null
    }

    fun providePath(path: PlanetPath) {
        val type = requestType ?: return
        if (type != Type.POINT_END) return

        @Suppress("UNCHECKED_CAST") val cont =
            (cont ?: return) as CancellableContinuation<PlanetPath?>

        cont.resume(path)

        this.requestType = null
        this.cont = null
    }

    fun cancelRequest() {
        requestType = null
        cont?.cancel()
        cont = null
    }

    private suspend fun <T> startRequest(type: Type): T {
        requestType = type
        return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
            this.cont = cont
        }
    }

    suspend fun requestPoint(): PlanetPoint? {
        cancelRequest()
        return startRequest(Type.POINT)
    }

    suspend fun requestPointEnd(): Pair<PlanetPoint, PlanetDirection>? {
        cancelRequest()
        return startRequest(Type.POINT_END)
    }

    suspend fun requestPath(): PlanetPath? {
        cancelRequest()
        return startRequest(Type.PATH)
    }
}
