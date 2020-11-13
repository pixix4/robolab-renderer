package de.robolab.server.routes

import de.robolab.client.net.requests.auth.TokenLinkPair
import de.robolab.common.auth.User
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.server.net.RESTResponseCodeException
import de.robolab.server.auth.AuthService
import de.robolab.server.auth.GitLabAuthProvider
import de.robolab.server.auth.ShareCode
import de.robolab.server.config.Config
import de.robolab.server.externaljs.NodeError
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.externaljs.express.*

object AuthRouter {
    val router: DefaultRouter = createRouter()
    val authProvider: GitLabAuthProvider = GitLabAuthProvider("${Config.Auth.hostURL}/api/auth/gitlab/redirect")
    val authService: AuthService = AuthService()
    val allowedInvalidTokenRoutes: Set<String> = setOf(
        "/version",
        "/info/status",
        "/auth/gitlab",
        "/auth/gitlab/relay",
        "/auth/gitlab/redirect",
        "/auth/token",
        "/auth/clear",
    )

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
            val stateString: String = dynState as? String ?: throw RESTResponseCodeException(
                HttpStatusCode.BadRequest,
                "Missing State-parameter"
            )
            val shareCode: ShareCode
            shareCode = authProvider.extractShareCode(stateString) ?: throw RESTResponseCodeException(
                HttpStatusCode.BadRequest,
                "State-parameter is not valid"
            )
            authService.assertCanProvide(shareCode)
            if (code == null) {
                authService.abortShare(shareCode)
                res.sendStatus(HttpStatusCode.Unauthorized)
            } else {
                val user = authProvider.performAuth(code, stateString, shareCode)
                val token = authService.obtainToken(user)
                res.cookie("robolab_auth", token.rawToken, dynamicOf("httpOnly" to true))
                if (authService.provideSharedToken(shareCode, token, user.userID)) {
                    //Code is used in a share-process, JWT has already been passed on
                    res.status(HttpStatusCode.Ok).type(MIMEType.HTML).send(
                        """<!DOCTYPE html>
<html>
<body>
<script>
window.close();
</script>
</body>
</html>
""".trimIndent()
                    )
                } else {
                    //Code is not used in a share-process, return JWT
                    res.redirect(Config.Auth.redirectURL)
                }
            }
            return@getSuspend
        }
        router.getSuspend("/clear") { _, res ->
            res.clearCookie("robolab_auth")
            res.sendStatus(HttpStatusCode.NoContent)
        }
        router.getSuspend("/gitlab/relay") { _, res ->
            val shareCode = authService.createShareCode(true)
            res.sendSerializable(
                TokenLinkPair(
                    login = authProvider.startAuthURL(shareCode),
                    token = requestTokenURL(shareCode)
                )
            )
        }
        router.getSuspend("/gitlab/relay/html"){ _, res ->
            val shareCode = authService.createShareCode(true)
            res.sendSerializable(
                TokenLinkPair(
                    login = authProvider.startAuthURL(shareCode),
                    token = requestTokenURL(shareCode)
                ), MIMEType.HTML
            )
        }
        router.getSuspend("/gitlab/token") { req, res ->
            val dynState = req.query.state
            val state = dynState as? String ?: throw RESTResponseCodeException(
                HttpStatusCode.BadRequest,
                "Missing State-parameter"
            )
            val shareCode: ShareCode = authProvider.extractShareCode(state) ?: throw RESTResponseCodeException(
                HttpStatusCode.BadRequest,
                "State-parameter is not valid"
            )
            val token = authService.getSharedToken(shareCode)
            if (token == null) {
                res.sendStatus(HttpStatusCode.NoContent)
            } else {
                res.status(HttpStatusCode.Ok).type(MIMEType.JWT).send(token.rawToken)
            }
        }
    }

    fun requestTokenURL(shareCode: ShareCode): String {
        return "${Config.Auth.hostURL}/api/auth/gitlab/token?state=$shareCode"
    }

    fun userLookupMiddleware(req: Request<*>, res: Response<*>, next: (NodeError?) -> Unit) {
        var authHeaderValue: String? = req.headers[AuthorizationHeader.name] as? String
        val authCookie = req.cookies?.robolab_auth as? String
        val actualHeaderPresent: Boolean = authHeaderValue != null
        if (authHeaderValue == null) {
            if (authCookie == null) {
                req.user = User.Anonymous
                res.setHeader("robolab-user", req.user.internalName)
                return next(null)
            } else {
                authHeaderValue = "Bearer $authCookie"
            }
        }
        var authHeader: AuthorizationHeader = AuthorizationHeader.parse(authHeaderValue)
        if (authHeader !is AuthorizationHeader.Bearer) {
            if (actualHeaderPresent) {
                if (authCookie == null) {
                    req.user = User.Anonymous
                    res.setHeader("robolab-user", req.user.internalName)
                    return next(null)
                } else {
                    authHeaderValue = "Bearer $authCookie"
                    authHeader = AuthorizationHeader.parse(authHeaderValue)
                    if (authHeader !is AuthorizationHeader.Bearer) { //TODO: fall-through into BEARER-Schema-Requirement again
                        req.user = User.Anonymous
                        res.setHeader("robolab-user", req.user.internalName)
                        return next(null)
                    }
                }
            } else { //TODO: fall-through into BEARER-Schema-Requirement again
                req.user = User.Anonymous
                res.setHeader("robolab-user", req.user.internalName)
                return next(null)
            }
            res.setHeader("robolab-error", "Authorization requires the \"Bearer\"-Schema")
            res.sendStatus(HttpStatusCode.Unauthorized)
            return
        }
        val headerUser = authService.obtainUser(authHeader)
            ?: if (req.path in allowedInvalidTokenRoutes) User.Anonymous else null
        if (headerUser == null) {
            res.setHeader("robolab-error", "Invalid/Expired Bearer-Token")
            res.sendStatus(HttpStatusCode.Unauthorized)
            return
        } else {
            req.user = headerUser
            res.setHeader(
                "robolab-user", req.user.internalName
            )
            return next(null)
        }
    }
}