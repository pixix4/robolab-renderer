package de.robolab.server.externaljs.express

import de.robolab.server.externaljs.Buffer
import de.robolab.server.externaljs.JSArray
import de.robolab.server.externaljs.JSDynErrorCallback
import de.robolab.server.externaljs.NodeError

val Express = js("require(\"express\")")

fun createApp(): ExpressApp = Express()
    .unsafeCast<ExpressApp>()

fun createRouter(): Router = Express.Router().unsafeCast<Router>()

external interface Request : de.robolab.server.externaljs.http.IncomingServerMessage {
    val app: ExpressApp
    val baseUrl: String
    val body: dynamic
    val cookies: dynamic
    val fresh: Boolean
    val hostname: String
    val ip: String
    val ips: JSArray<String>
    val originalUrl : String
    val params: dynamic
    val path: String
    val protocol: String
    val query: dynamic
    val secure: Boolean
    val signedCookies: dynamic
    val stale: Boolean
    val subdomains: JSArray<String>
    val xhr: Boolean

    fun accepts(types: String): Any
    fun accepts(types: List<String>): Any

    fun acceptsCharsets(charset: String): Any
    fun acceptsCharsets(charset: JSArray<String>): Any

    fun acceptsEncoding(charset: String): Any
    fun acceptsEncoding(charset: JSArray<String>): Any

    fun acceptsLanguage(charset: String): Any
    fun acceptsLanguage(charset: JSArray<String>): Any

    fun get(field:String): String?

    fun param(name:String): String?
    fun param(name:String,defaultValue:String):String

    fun range(size:Int): Any
}

private val isMimeTypeProto = Express.request["is"]

fun Request.isMimeType(type:String):Any{
    return isMimeTypeProto(this,type) as Any
}

external interface Response: de.robolab.server.externaljs.http.ServerResponse{
    val app: ExpressApp
    val locals: dynamic

    fun append(field:String)
    fun append(field:String, value: String)
    fun append(field:String, value: JSArray<String>)

    fun attachment()
    fun attachment(fileName:String)

    fun cookie(name:String,value:String)
    fun cookie(name:String,value:String,options:dynamic)

    fun clearCookie(name:String)
    fun clearCookie(name:String, options:dynamic)

    fun download(path:String)
    fun download(path:String, fileName:String)
    fun download(path:String, fileName:String, options:dynamic)
    fun download(path:String, fileName:String, options:dynamic, fn: JSDynErrorCallback)

    fun format(obj:dynamic)

    fun get(field:String):String

    fun json()
    fun json(body:dynamic)

    fun jsonp()
    fun jsonp(body:dynamic)

    fun links(links:dynamic)

    fun location(path:String)

    fun redirect(path:String)
    fun redirect(status:Int, path:String)

    fun send()
    fun send(body: String)
    fun send(body:dynamic)
    fun send(body:JSArray<*>)
    fun send(body: Buffer)

    fun sendFile(path:String)
    fun sendFile(path:String, options:dynamic)
    fun sendFile(path:String, options:dynamic, fn:JSDynErrorCallback)

    fun sendStatus(statusCode:Int)

    fun set(field:String, value:dynamic)

    fun status(code:Int) : Response

    fun type(type:String): Response

    fun vary(field:String):Response
}

typealias TerminalMiddleware = (req:Request, res:Response) -> Unit
typealias SimpleMiddleware = (req:Request, res:Response, next:()->Unit) -> Unit
typealias Middleware = (req: Request, res: Response, next:(NodeError?)->Unit) -> Unit
typealias ParamCallback = (req: Request, res: Response, next:(NodeError?)->Unit, value:String) -> Unit
typealias NamedParamCallback = (req: Request, res: Response, next:(NodeError?)->Unit, value:String, name:String)->Unit

abstract external class Router{
    fun all(path:String, callback: TerminalMiddleware)
    fun all(path:String, callback: SimpleMiddleware)
    fun all(path:String, callback: Middleware)
    fun all(path:String, callback: Router)
    fun checkout(path:String, callback: TerminalMiddleware)
    fun checkout(path:String, callback: SimpleMiddleware)
    fun checkout(path:String, callback: Middleware)
    fun checkout(path:String, callback: Router)
    fun copy(path:String, callback: TerminalMiddleware)
    fun copy(path:String, callback: SimpleMiddleware)
    fun copy(path:String, callback: Middleware)
    fun copy(path:String, callback: Router)
    fun delete(path:String, callback: TerminalMiddleware)
    fun delete(path:String, callback: SimpleMiddleware)
    fun delete(path:String, callback: Middleware)
    fun delete(path:String, callback: Router)
    fun get(path:String, callback: TerminalMiddleware)
    fun get(path:String, callback: SimpleMiddleware)
    fun get(path:String, callback: Middleware)
    fun get(path:String, callback: Router)
    fun head(path:String, callback: TerminalMiddleware)
    fun head(path:String, callback: SimpleMiddleware)
    fun head(path:String, callback: Middleware)
    fun head(path:String, callback: Router)
    fun lock(path:String, callback: TerminalMiddleware)
    fun lock(path:String, callback: SimpleMiddleware)
    fun lock(path:String, callback: Middleware)
    fun lock(path:String, callback: Router)
    fun merge(path:String, callback: TerminalMiddleware)
    fun merge(path:String, callback: SimpleMiddleware)
    fun merge(path:String, callback: Middleware)
    fun merge(path:String, callback: Router)
    fun mkactivity(path:String, callback: TerminalMiddleware)
    fun mkactivity(path:String, callback: SimpleMiddleware)
    fun mkactivity(path:String, callback: Middleware)
    fun mkactivity(path:String, callback: Router)
    fun mkcol(path:String, callback: TerminalMiddleware)
    fun mkcol(path:String, callback: SimpleMiddleware)
    fun mkcol(path:String, callback: Middleware)
    fun mkcol(path:String, callback: Router)
    fun move(path:String, callback: TerminalMiddleware)
    fun move(path:String, callback: SimpleMiddleware)
    fun move(path:String, callback: Middleware)
    fun move(path:String, callback: Router)
    fun notify(path:String, callback: TerminalMiddleware)
    fun notify(path:String, callback: SimpleMiddleware)
    fun notify(path:String, callback: Middleware)
    fun notify(path:String, callback: Router)
    fun options(path:String, callback: TerminalMiddleware)
    fun options(path:String, callback: SimpleMiddleware)
    fun options(path:String, callback: Middleware)
    fun options(path:String, callback: Router)
    fun patch(path:String, callback: TerminalMiddleware)
    fun patch(path:String, callback: SimpleMiddleware)
    fun patch(path:String, callback: Middleware)
    fun patch(path:String, callback: Router)
    fun post(path:String, callback: TerminalMiddleware)
    fun post(path:String, callback: SimpleMiddleware)
    fun post(path:String, callback: Middleware)
    fun post(path:String, callback: Router)
    fun purge(path:String, callback: TerminalMiddleware)
    fun purge(path:String, callback: SimpleMiddleware)
    fun purge(path:String, callback: Middleware)
    fun purge(path:String, callback: Router)
    fun put(path:String, callback: TerminalMiddleware)
    fun put(path:String, callback: SimpleMiddleware)
    fun put(path:String, callback: Middleware)
    fun put(path:String, callback: Router)
    fun report(path:String, callback: TerminalMiddleware)
    fun report(path:String, callback: SimpleMiddleware)
    fun report(path:String, callback: Middleware)
    fun report(path:String, callback: Router)
    fun search(path:String, callback: TerminalMiddleware)
    fun search(path:String, callback: SimpleMiddleware)
    fun search(path:String, callback: Middleware)
    fun search(path:String, callback: Router)
    fun subscribe(path:String, callback: TerminalMiddleware)
    fun subscribe(path:String, callback: SimpleMiddleware)
    fun subscribe(path:String, callback: Middleware)
    fun subscribe(path:String, callback: Router)
    fun trace(path:String, callback: TerminalMiddleware)
    fun trace(path:String, callback: SimpleMiddleware)
    fun trace(path:String, callback: Middleware)
    fun trace(path:String, callback: Router)
    fun unlock(path:String, callback: TerminalMiddleware)
    fun unlock(path:String, callback: SimpleMiddleware)
    fun unlock(path:String, callback: Middleware)
    fun unlock(path:String, callback: Router)
    fun unsubscribe(path:String, callback: TerminalMiddleware)
    fun unsubscribe(path:String, callback: SimpleMiddleware)
    fun unsubscribe(path:String, callback: Middleware)
    fun unsubscribe(path:String, callback: Router)

    fun param(name:String, callback: ParamCallback)
    fun param(name:String, callback: NamedParamCallback)
    fun route(path:String): Router
    fun use(function: Router)
    fun use(function: Middleware)
    fun use(path:String, function: Middleware)
    fun use(path:String, function: Router)
}

abstract external class ExpressApp : Router {
    val locals: dynamic
    val mountpath: Any
    fun disable(name:String):Unit
    fun disabled(name:String):Boolean
    fun enable(name:String):Unit
    fun enabled(name:String):Boolean
    fun path():String
    operator fun get(name:String):Any?
    operator fun set(name:String,value:Any?)
}