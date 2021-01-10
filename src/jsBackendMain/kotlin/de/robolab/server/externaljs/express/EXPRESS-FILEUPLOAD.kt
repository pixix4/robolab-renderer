package de.robolab.server.externaljs.express

private val module = js("require(\"express-fileupload\")")

fun fileUpload(): Middleware<*, *> {
    return module() as Middleware<*, *>
}
