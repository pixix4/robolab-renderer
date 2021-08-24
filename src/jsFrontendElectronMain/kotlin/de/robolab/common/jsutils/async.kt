package de.robolab.common.jsutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlin.js.Promise

val PromiseScope: CoroutineScope = MainScope()

fun <R> promise(block: suspend CoroutineScope.() -> R): Promise<R> = PromiseScope.async { block(this) }.asPromise()