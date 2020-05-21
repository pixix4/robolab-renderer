package de.robolab.server.externaljs

typealias JSBiCallback<E,T> = (err:E,res:T)->Unit
typealias JSErrorCallback<E> = (err:E)->Unit
typealias JSDynErrorCallback = JSErrorCallback<dynamic>
typealias JSBiDynErrorCallback<T> = JSBiCallback<dynamic, T>
typealias JSDynBiCallback = JSBiDynErrorCallback<dynamic>