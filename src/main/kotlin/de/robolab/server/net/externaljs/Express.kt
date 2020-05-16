package de.robolab.server.net.externaljs

val Express = js("require(\"express\")")

fun createApp():dynamic = Express()