package de.robolab.server.model

import de.robolab.common.planet.ID
import de.robolab.server.externaljs.Buffer

const val IDEncoding: String = "ascii"

fun ID.decode(): String = Buffer.from(id, "base64").toString(
    IDEncoding
)

fun String.toID(): ID = ID(
    Buffer.from(
        this,
        IDEncoding
    ).toString("base64")
)