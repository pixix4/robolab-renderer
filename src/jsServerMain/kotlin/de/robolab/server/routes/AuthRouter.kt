package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.server.RequestError
import de.robolab.server.auth.AuthService
import de.robolab.server.auth.GitLabAuthProvider
import de.robolab.server.auth.ShareCode
import de.robolab.server.auth.User
import de.robolab.server.config.Config
import de.robolab.server.externaljs.NodeError
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.externaljs.express.*
import de.robolab.server.externaljs.jsonwebtoken.parseJWT
import org.w3c.dom.MimeType

object AuthRouter {
    val router: DefaultRouter = createRouter()
    val authProvider: GitLabAuthProvider = GitLabAuthProvider("${Config.Auth.hostURL}/api/auth/gitlab/redirect")
    val authService: AuthService = AuthService(false)


    init {
        router.getSuspend("/gitlab") { _, res ->
            val shareCode = authService.createShareCode(false)
            val targetURL: String = authProvider.startAuthURL(shareCode)
            res.redirect(targetURL)
        }
        router.getSuspend("/gitlab/redirect") { req, res ->
            val dynCode: dynamic = req.query.code
            val dynState: dynamic = req.query.state
            val code: String? = dynCode as? String
            val stateString: String = dynState as? String ?: throw RequestError(HttpStatusCode.BadRequest, "Missing State-parameter",verbose=false)
            val shareCode: ShareCode
            shareCode = authProvider.extractShareCode(stateString) ?: throw RequestError(HttpStatusCode.BadRequest,"State-parameter is not valid")
            authService.assertCanProvide(shareCode)
            if (code == null) {
                authService.abortShare(shareCode)
                res.sendStatus(HttpStatusCode.Unauthorized)
            } else {
                val user = authProvider.performAuth(code, stateString, shareCode)
                val token = authService.obtainToken(user);
                if (authService.provideSharedToken(shareCode, token, user.userID)) {
                    //Code is used in a share-process, JWT has already been passed on
                    res.status(HttpStatusCode.NoContent).end() //TODO: Close page
                } else {
                    //Code is not used in a share-process, return JWT
                    res.status(HttpStatusCode.Ok).type(MIMEType.JWT)
                        .send(token.rawToken) //TODO: Set auth-header and redirect to index
                }
            }
            return@getSuspend
        }
        router.getSuspend("/gitlab/relay") { _, res ->
            val shareCode = authService.createShareCode(true)
            res.json(
                dynamicOf(
                    "login" to authProvider.startAuthURL(shareCode),
                    "token" to requestTokenURL(shareCode)
                )
            )
        }
        router.getSuspend("/gitlab/token") { req, res ->
            val dynState = req.query.state
            val state = dynState as? String ?: throw RequestError(HttpStatusCode.BadRequest, "Missing State-parameter",verbose=false)
            val shareCode: ShareCode = authProvider.extractShareCode(state) ?: throw RequestError(HttpStatusCode.BadRequest,"State-parameter is not valid")
            val token = authService.getSharedToken(shareCode)
            if(token == null){
                res.sendStatus(HttpStatusCode.Unauthorized)
            }else{
                res.status(HttpStatusCode.Ok).type(MIMEType.JWT).send(token.rawToken)
            }
        }
    }

    fun requestTokenURL(shareCode: ShareCode): String {
        return "${Config.Auth.hostURL}/api/auth/gitlab/token?state=$shareCode"
    }

    fun userLookupMiddleware(req: Request<*>, res: Response<*>, next: (NodeError?) -> Unit) {
        val authHeaderValue: String? = req.headers[AuthorizationHeader.name] as? String
        if (authHeaderValue == null) {
            req.user = User.Anonymous
            res.setHeader("robolab-user", "anonymous")
            return next(null)
        }
        val authHeader: AuthorizationHeader = AuthorizationHeader.parse(authHeaderValue)
        if (authHeader !is AuthorizationHeader.Bearer) {
            res.setHeader("robolab-error", "Authorization requires the \"Bearer\"-Schema")
            res.sendStatus(HttpStatusCode.Unauthorized)
            return
        }
        val headerUser = authService.obtainUser(authHeader)
        if (headerUser == null) {
            res.setHeader("robolab-error", "Invalid/Expired Bearer-Token")
            res.sendStatus(HttpStatusCode.Unauthorized)
            return
        } else {
            req.user = headerUser
            res.setHeader("robolab-user",
                if(req.user == User.Anonymous)
                    "anonymous"
                else
                    req.user.userID.toString())
            return next(null)
        }
    }
}