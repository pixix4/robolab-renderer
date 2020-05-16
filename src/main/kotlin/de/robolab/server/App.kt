package de.robolab.server

import de.robolab.server.net.DefaultEnvironment

fun main(){
    DefaultEnvironment.app.get("/") { req, res ->
        res.status(200).send("Hello world!")
    }
    DefaultEnvironment.http.listen(80) {
        console.log("listening on port 80")
    }
    console.log("Hello, Kotlin/JS!")
}