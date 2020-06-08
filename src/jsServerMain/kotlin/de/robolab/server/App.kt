package de.robolab.server

import de.robolab.server.net.DefaultEnvironment
/*import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.externaljs.jsArrayOf
import de.robolab.server.externaljs.mongoose.Schema
import de.robolab.server.externaljs.mongoose.connectOptions
import de.robolab.server.externaljs.mongoose.mongoose
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch*/


fun main() {

    /*GlobalScope.launch {
        mongoose.connectOptions("mongodb://localhost:27017").await()
        println("Connected to mongo")
        val testModel = mongoose.model(
            "Test", Schema(
                dynamicOf(
                    "planet" to Schema.Companion.Types.String
                ).unsafeCast<Any?>()
            )
        )
        testModel.create(jsArrayOf(dynamicOf("planet" to "Hello")))
    }*/

    DefaultEnvironment.app.use("/api", DefaultEnvironment.createApiRouter())
    DefaultEnvironment.app.get("/") { _, res ->
        res.status(200).send("Hello world!")
    }
    DefaultEnvironment.http.listen(8080) {
        console.log("listening on port 8080")
    }
    console.log("\n\n\tHello, Kotlin/JS!\n\n")
}