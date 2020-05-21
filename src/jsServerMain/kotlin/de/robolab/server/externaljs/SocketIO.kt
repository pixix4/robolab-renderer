package de.robolab.server.externaljs

val SocketIO = js("require(\"socket.io\")")

fun createIO(http:dynamic):dynamic = SocketIO(http)