package de.robolab.server.externaljs.body_parser

import de.robolab.common.net.MIMEType
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.externaljs.express.Middleware

private val module = js("require(\"body-parser\")")

fun json(
    inflate: Boolean = true,
    limit: String = "100kb",
    strict: Boolean = true,
    type: String = MIMEType.JSON.primaryName
): Middleware = module.json(
    dynamicOf(
        "inflate" to inflate,
        "limit" to limit,
        "strict" to strict,
        "type" to type
    )
).unsafeCast<Middleware>()

fun raw(
    inflate: Boolean = true,
    limit: String = "100kb",
    type: String = MIMEType.OCTET_STREAM.primaryName
): Middleware = module.raw(
    dynamicOf(
        "inflate" to inflate,
        "limit" to limit,
        "type" to type
    )
).unsafeCast<Middleware>()

fun text(
    defaultCharset: String = "utf-8",
    inflate: Boolean = true,
    limit: String = "100kb",
    type: String = MIMEType.PlainText.primaryName
): Middleware = module.text(
    dynamicOf(
        "defaultCharset" to defaultCharset,
        "inflate" to inflate,
        "limit" to limit,
        "type" to type
    )
).unsafeCast<Middleware>()

fun urlencoded(
    extended: Boolean,
    inflate: Boolean = true,
    limit: String = "100kb",
    parameterLimit: Int = 1000,
    type: String = "application/x-www-form-urlencoded"
): Middleware = module.urlencoded(
    dynamicOf(
        "extended" to extended,
        "inflate" to inflate,
        "limit" to limit,
        "parameterLimit" to parameterLimit,
        "type" to type
    )
).unsafeCast<Middleware>()
