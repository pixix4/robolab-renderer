package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.server.auth.GitLabAuthHandler
import de.robolab.server.config.Config
import de.robolab.server.externaljs.express.*

object AuthRouter {
    val router: DefaultRouter = createRouter()
    val authHandler: GitLabAuthHandler = GitLabAuthHandler("${Config.Auth.hostURL}/api/auth/gitlab/redirect")

    init {
        router.getSuspend("/gitlab") { req, res ->
            val targetURL: String = authHandler.startAuthURL()
            res.redirect(targetURL)
        }
        router.getSuspend("/gitlab/redirect") { req, res ->
            val dynCode: dynamic = req.query.code
            val dynState: dynamic = req.query.state
            val code: String? = dynCode as? String
            val stateString: String = dynState as String
            if(code == null){
                res.sendStatus(HttpStatusCode.Unauthorized)
            }else{
                val user = authHandler.performAuth(code, stateString.toUInt())
                if (user.isRoboLab) {
                    res.sendStatus(HttpStatusCode.Ok)
                } else {
                    res.sendStatus(HttpStatusCode.Forbidden)
                }
            }
            return@getSuspend
        }
    }
}