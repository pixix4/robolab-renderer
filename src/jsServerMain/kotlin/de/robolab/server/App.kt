package de.robolab.server

import de.robolab.common.utils.ConsoleGreeter
import de.robolab.common.utils.Logger
import de.robolab.server.net.DefaultEnvironment
import de.robolab.server.routes.logoResponse

/*import de.robolab.server.externaljs.ioredis.createRedis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch*/


fun main() {

    /*GlobalScope.launch {
        val redis = createRedis()
        println("Connected to redis")
        //println(redis.getBuiltinCommands())
        console.log("redis keys:")
        console.log(redis.keys("*").await())
        redis.lpush("list","Hello").await()
        println("prevsave ${redis.lastsave().await()}")
        redis.save()
        println("nowsave ${redis.lastsave().await()}")
        println("list is now ${redis.lrange("list",0,-1).await()}")
    }*/

    ConsoleGreeter.greetServer()
    val logger = Logger("MainApp")

    DefaultEnvironment.app.use("/api", DefaultEnvironment.createApiRouter())
    DefaultEnvironment.app.get("/", logoResponse)
    DefaultEnvironment.http.listen(8080) {
        logger.i("Listening on port 8080")
    }
}
