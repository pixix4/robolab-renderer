package de.robolab.server.net

import de.robolab.server.externaljs.express.ExpressApp
import de.robolab.server.externaljs.express.createApp
import de.robolab.server.externaljs.createIO
import de.robolab.server.externaljs.http.createServer

object DefaultEnvironment {
    val app: ExpressApp = createApp()
    val http: dynamic = createServer(app)
    val io: dynamic = createIO(http)
}