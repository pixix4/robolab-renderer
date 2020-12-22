package de.robolab.server.externaljs.cookie_parser

import de.robolab.server.externaljs.express.Middleware

private val module = js("require(\"cookie-parser\")")

fun cookieParser(): Middleware<*,*>{
    return module() as Middleware<*, *>
}