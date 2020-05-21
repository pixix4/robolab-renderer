package de.robolab.server

import de.robolab.server.net.DefaultEnvironment
import de.robolab.server.externaljs.fs.*

fun main(){
    DefaultEnvironment.app.get("/") { req, res ->
        res.status(200).send("Hello world!")
    }
    DefaultEnvironment.http.listen(8080) {
        console.log("listening on port 8080")
    }
    val entries = readdirSync(".")
    for (t in entries.map{it1-> it1 to statSync(it1).let { listOf(it.atime,it.ctime,it.mtime,it.size,it.isDirectory(),it.isFile()) }})
        console.log(t)
    console.log("\n\n\tHello, Kotlin/JS!\n\n")
}