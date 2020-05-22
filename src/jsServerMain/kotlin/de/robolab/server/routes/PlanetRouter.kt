package de.robolab.server.routes

import de.robolab.server.externaljs.express.Router
import de.robolab.server.externaljs.express.createRouter

object PlanetRouter {
    val router: Router = createRouter()

    init{
        router.get("/"){req, res->
            res.status(200).type("application/json").send("[]")
        }
        router.get("/:id"){req, res->
            res.status(404).send("Planet with id '"+req.param("id") +"' could not be found")
        }
        router.post("/"){req,res->
            res.status(201).type("application/json").send("\"SUCH-RANDOM-VERY-ID-WOW\"")
        }
        router.put("/:id"){req, res->
            res.status(404).send("Planet with id '"+req.param("id")+"' could not be found")
        }
    }
}