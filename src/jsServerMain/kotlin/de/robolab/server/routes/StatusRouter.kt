package de.robolab.server.routes

import de.robolab.common.utils.ConsoleGreeter
import de.robolab.server.externaljs.express.TerminalMiddleware

val logoResponse: TerminalMiddleware = { req, res ->
    val accept = req.headers["accept"] as? String

    val message = "${ConsoleGreeter.appLogo}\n${ConsoleGreeter.appServerCreators}"
    if (accept?.contains("text/html", true) == true) {
        res.status(200).send("<pre>\n$message\n</pre>")
    } else {
        res.status(200).send(message)
    }
}