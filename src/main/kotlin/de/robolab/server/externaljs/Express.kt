package de.robolab.server.externaljs

val Express = js("require(\"express\")")

fun createApp():dynamic = Express()