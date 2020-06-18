package de.robolab.server.model

import de.robolab.common.planet.ID
import de.robolab.server.externaljs.Buffer

const val IDEncoding: String = "ascii"

fun ID.decode(): String = Buffer.from(
    id
        .replace('-', '+')
        .replace('_', '/')
        .replace("%3d", "="), "base64"
).toString(
    IDEncoding
)

fun String.toID(): ID = ID(
    Buffer.from(
        this,
        IDEncoding
    ).toString("base64")
        .replace('+', '-')
        .replace('/', '_')
        .replace("""=+$""".toRegex(), "") //removes all trailing '=', which should be all
        .replace("=", "%3d")
)

fun String.toIDString(): String = this.toID().id
fun String.decodeID(): String = ID(this).decode()