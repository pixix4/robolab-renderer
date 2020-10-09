package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.server.auth.AuthService
import de.robolab.server.auth.GitLabAuthProvider
import de.robolab.server.auth.User
import de.robolab.server.config.Config
import de.robolab.server.externaljs.NodeError
import de.robolab.server.externaljs.express.*
import de.robolab.server.externaljs.jsonwebtoken.parseJWT

object AuthRouter {
    val router: DefaultRouter = createRouter()
    val authProvider: GitLabAuthProvider = GitLabAuthProvider("${Config.Auth.hostURL}/api/auth/gitlab/redirect")
    val authService: AuthService = AuthService()



    init {
        router.getSuspend("/gitlab") { req, res ->
            val targetURL: String = authProvider.startAuthURL()
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
                val user = authProvider.performAuth(code, stateString.toUInt())
                val token = authService.obtainToken(user);
                res.status(HttpStatusCode.Ok).send(token.rawToken)
            }
            return@getSuspend
        }
    }

    fun userLookupMiddleware(req : Request<*>, res: Response<*>, next: (NodeError?)->Unit){
        val authHeaderValue: String? = req.headers[AuthorizationHeader.name] as? String
        if(authHeaderValue == null){
            req.user = User.Anonymous
            res.setHeader("robolab-user", req.user.userID.toString())
            return next(null)
        }
        val authHeader: AuthorizationHeader = AuthorizationHeader.parse(authHeaderValue)
        if(authHeader !is AuthorizationHeader.Bearer)
        {
            res.setHeader("robolab-error","Authorization requires the \"Bearer\"-Schema")
            res.sendStatus(HttpStatusCode.Unauthorized)
            return
        }
        val headerUser = authService.obtainUser(authHeader)
        if(headerUser == null){
            res.setHeader("robolab-error","Invalid/Expired Bearer-Token")
            res.sendStatus(HttpStatusCode.Unauthorized)
            return
        }else{
            req.user = headerUser
            res.setHeader("robolab-user", req.user.userID.toString())
            return next(null)
        }
    }
}