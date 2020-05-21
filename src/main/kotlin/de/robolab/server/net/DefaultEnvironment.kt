package de.robolab.server.net

import de.robolab.server.externaljs.createApp
import de.robolab.server.externaljs.createIO
import de.robolab.server.externaljs.createServer

object DefaultEnvironment {
    val app: dynamic = createApp()
    val http: dynamic = createServer(app)
    val io: dynamic = createIO(http)
}