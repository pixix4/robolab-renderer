package de.robolab.server

import de.robolab.server.net.DefaultEnvironment

fun main() {
    DefaultEnvironment.app.use("/api", DefaultEnvironment.createApiRouter())
    DefaultEnvironment.app.get("/") { _, res ->
        res.status(200).send("Hello world!")
    }
    DefaultEnvironment.http.listen(8080) {
        console.log("listening on port 8080")
    }
    console.log("\n\n\tHello, Kotlin/JS!\n\n")
}