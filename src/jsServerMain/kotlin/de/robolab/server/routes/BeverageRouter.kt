package de.robolab.server.routes

import de.robolab.server.config.Config
import de.robolab.server.externaljs.express.Router
import de.robolab.server.externaljs.express.createRouter
import de.robolab.server.jsutils.jsTruthy

object BeverageRouter {
    val mateRouter: Router = createRouter()
    val teaRouter: Router = createRouter()
    val coffeeRouter: Router = createRouter()

    init {
        mateRouter.get("/") { _, res ->
            val targetURL: String? = Config.Beverage.payPalMateURL
            if (targetURL.jsTruthy())
                res.redirect(402, targetURL!!)
            else {
                res.status(402).send(Config.Beverage.payPalMateText)
            }
        }
        teaRouter.get("/") { _, res ->
            res.sendStatus(501)
        }
        coffeeRouter.get("/") { _, res ->
            res.sendStatus(418)
        }
    }
}