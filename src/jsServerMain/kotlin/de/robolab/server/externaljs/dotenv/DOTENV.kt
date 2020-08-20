package de.robolab.server.externaljs.dotenv

private var wasSetup: Boolean = false
private val module: dynamic = {
    val module: dynamic = js("require(\"dotenv\")")
    if (!wasSetup) {
        module.config()
        wasSetup = true
    }
    module
}()

fun config(env: dynamic = undefined) {
    if (!wasSetup) {
        module.config()
        wasSetup = true
    }
}