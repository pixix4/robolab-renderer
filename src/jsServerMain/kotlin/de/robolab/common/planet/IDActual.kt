package de.robolab.common.planet

import de.robolab.server.externaljs.Buffer

const val IDEncoding: String = "ascii"

actual fun ID.decode(): String = Buffer.from(id, "base64").toString(IDEncoding)

actual fun String.toID(): ID = ID(Buffer.from(this, IDEncoding).toString("base64"))