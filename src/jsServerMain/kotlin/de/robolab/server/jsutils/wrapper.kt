@file:Suppress("NOTHING_TO_INLINE")

package de.robolab.server.jsutils

typealias JSBiCallback<E, T> = (err: E, res: T) -> Unit
typealias JSErrorCallback<E> = (err: E) -> Unit
typealias JSDynErrorCallback = JSErrorCallback<dynamic>
typealias JSBiDynErrorCallback<T> = JSBiCallback<dynamic, T>
typealias JSDynBiCallback = JSBiDynErrorCallback<dynamic>

inline fun Any?.hasJSValue() = (this != null) && (this != undefined)

inline fun Any?.isUndefined() = this == undefined

inline fun Any?.isNull() = this == null

inline fun Any?.truthy() = !this.falsy()

@Suppress("DUPLICATE_LABEL_IN_WHEN")
fun Any?.falsy() = when (this) {
    false -> true
    0 -> true
    -0 -> true
    "" -> true
    null -> true
    undefined -> true
    is Double -> this.isNaN()
    is Float -> this.isNaN()
    else -> false
}

private val jsTruthyTest: (Any?) -> Boolean = js(
    """function jsTruthy(val){return !!val}"""
) as ((Any?) -> Boolean)

fun Any?.jsFalsy() = !jsTruthyTest(this)
fun Any?.jsTruthy() = jsTruthyTest(this)