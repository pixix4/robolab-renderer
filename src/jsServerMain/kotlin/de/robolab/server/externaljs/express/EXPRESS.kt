package de.robolab.server.externaljs.express

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.server.RequestError
import de.robolab.server.externaljs.Buffer
import de.robolab.server.externaljs.JSArray
import de.robolab.server.jsutils.JSDynErrorCallback
import de.robolab.server.externaljs.NodeError
import kotlinx.serialization.json.JsonElementSerializer
import kotlin.contracts.Returns
import kotlin.js.Promise

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
    val originalUrl: String
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

    fun get(field: String): String?

    fun param(name: String): String?
    fun param(name: String, defaultValue: String): String

    fun range(size: Int): Any
}

private val isMimeTypeProto = Express.request["is"]

fun Request.isMimeType(type: String): Any? {
    return isMimeTypeProto(this, type) as Any
}

fun Request.isMimeType(type: MIMEType): Any? {
    return isMimeType(type.primaryName).let {
        when (it) {
            false -> false
            type.primaryName -> type
            null -> null
            undefined -> undefined
            else -> MIMEType.parse(it as String)
        }
    }
}

val Request.mimeType: MIMEType?
    get() = contentTypeHeader?.mimeType

val Request.contentTypeHeader: ContentTypeHeader?
    get() {
        val headerValue: String? = this.headers[ContentTypeHeader.name] as? String
        return if (headerValue == null) null else ContentTypeHeader(headerValue)
    }

external interface Response : de.robolab.server.externaljs.http.ServerResponse {
    val app: ExpressApp
    val locals: dynamic

    fun append(field: String)
    fun append(field: String, value: String)
    fun append(field: String, value: JSArray<String>)

    fun attachment()
    fun attachment(fileName: String)

    fun cookie(name: String, value: String)
    fun cookie(name: String, value: String, options: dynamic)

    fun clearCookie(name: String)
    fun clearCookie(name: String, options: dynamic)

    fun download(path: String)
    fun download(path: String, fileName: String)
    fun download(path: String, fileName: String, options: dynamic)
    fun download(path: String, fileName: String, options: dynamic, fn: JSDynErrorCallback)

    fun format(obj: dynamic)

    fun get(field: String): String

    fun json()
    fun json(body: dynamic)

    fun jsonp()
    fun jsonp(body: dynamic)

    fun links(links: dynamic)

    fun location(path: String)

    fun redirect(path: String)
    fun redirect(status: Int, path: String)

    fun send()
    fun send(body: String)
    fun send(body: dynamic)
    fun send(body: JSArray<*>)
    fun send(body: Buffer)

    fun sendFile(path: String)
    fun sendFile(path: String, options: dynamic)
    fun sendFile(path: String, options: dynamic, fn: JSDynErrorCallback)

    fun sendStatus(statusCode: Int)

    fun set(field: String, value: dynamic)

    fun status(code: Int): Response

    fun type(type: String): Response

    fun vary(field: String): Response
}

fun Response.formatReceiving(vararg handlers: Pair<String, Response.() -> Unit>, defaultHandler: Response.() -> Unit) =
    format(*handlers.map { it.first to { it.second(this) } }.toTypedArray(), defaultHandler = { defaultHandler(this) })

fun Response.format(vararg handlers: Pair<String, () -> Unit>, defaultHandler: () -> Unit) =
    formatUnreceiving(*handlers, defaultHandler = defaultHandler)(this)

fun Response.format(vararg handlers: Pair<String, () -> Unit>) =
    this.format(*handlers, defaultHandler = { sendStatus(HttpStatusCode.NotAcceptable) })

fun Response.formatReceiving(vararg handlers: Pair<String, Response.() -> Unit>) =
    formatReceiving(*handlers, defaultHandler = { sendStatus(HttpStatusCode.NotAcceptable) })

fun formatReceiving(vararg handlers: Pair<String, Response.() -> Unit>): (Response) -> Unit =
    formatReceiving(*handlers, defaultHandler = { sendStatus(HttpStatusCode.NotAcceptable) })


fun formatReceiving(
    vararg handlers: Pair<String, Response.() -> Unit>,
    defaultHandler: Response.() -> Unit
): (Response) -> Unit {
    val obj: dynamic = js("{}")
    obj["default"] = defaultHandler
    handlers.forEach { obj[it.first] = it.second }
    return { it.format(obj) }
}

private fun formatUnreceiving(
    vararg handlers: Pair<String, () -> Unit>,
    defaultHandler: () -> Unit
): (Response) -> Unit {
    val obj: dynamic = js("{}")
    obj["default"] = defaultHandler
    handlers.forEach { obj[it.first] = it.second }
    return { it.format(obj) }
}

fun Response.status(status: HttpStatusCode): Response = this.status(status.code)

fun Response.sendStatus(status: HttpStatusCode) = this.sendStatus(status.code)

typealias TerminalMiddleware = (req: Request, res: Response) -> Unit
typealias TerminalPromiseCreator = (req: Request, res: Response) -> Promise<*>
typealias SimpleMiddleware = (req: Request, res: Response, next: () -> Unit) -> Unit
typealias Middleware = (req: Request, res: Response, next: (NodeError?) -> Unit) -> Unit
typealias ParamCallback = (req: Request, res: Response, next: (NodeError?) -> Unit, value: String) -> Unit
typealias NamedParamCallback = (req: Request, res: Response, next: (NodeError?) -> Unit, value: String, name: String) -> Unit

abstract external class Router {
    fun all(path: String, callback: TerminalMiddleware)
    fun all(path: String, callback: SimpleMiddleware)
    fun all(path: String, callback: Middleware)
    fun all(path: String, callback: Router)
    fun checkout(path: String, callback: TerminalMiddleware)
    fun checkout(path: String, callback: SimpleMiddleware)
    fun checkout(path: String, callback: Middleware)
    fun checkout(path: String, callback: Router)
    fun copy(path: String, callback: TerminalMiddleware)
    fun copy(path: String, callback: SimpleMiddleware)
    fun copy(path: String, callback: Middleware)
    fun copy(path: String, callback: Router)
    fun delete(path: String, callback: TerminalMiddleware)
    fun delete(path: String, callback: SimpleMiddleware)
    fun delete(path: String, callback: Middleware)
    fun delete(path: String, callback: Router)
    fun get(path: String, callback: TerminalMiddleware)
    fun get(path: String, callback: SimpleMiddleware)
    fun get(path: String, callback: Middleware)
    fun get(path: String, callback: Router)
    fun head(path: String, callback: TerminalMiddleware)
    fun head(path: String, callback: SimpleMiddleware)
    fun head(path: String, callback: Middleware)
    fun head(path: String, callback: Router)
    fun lock(path: String, callback: TerminalMiddleware)
    fun lock(path: String, callback: SimpleMiddleware)
    fun lock(path: String, callback: Middleware)
    fun lock(path: String, callback: Router)
    fun merge(path: String, callback: TerminalMiddleware)
    fun merge(path: String, callback: SimpleMiddleware)
    fun merge(path: String, callback: Middleware)
    fun merge(path: String, callback: Router)
    fun mkactivity(path: String, callback: TerminalMiddleware)
    fun mkactivity(path: String, callback: SimpleMiddleware)
    fun mkactivity(path: String, callback: Middleware)
    fun mkactivity(path: String, callback: Router)
    fun mkcol(path: String, callback: TerminalMiddleware)
    fun mkcol(path: String, callback: SimpleMiddleware)
    fun mkcol(path: String, callback: Middleware)
    fun mkcol(path: String, callback: Router)
    fun move(path: String, callback: TerminalMiddleware)
    fun move(path: String, callback: SimpleMiddleware)
    fun move(path: String, callback: Middleware)
    fun move(path: String, callback: Router)
    fun notify(path: String, callback: TerminalMiddleware)
    fun notify(path: String, callback: SimpleMiddleware)
    fun notify(path: String, callback: Middleware)
    fun notify(path: String, callback: Router)
    fun options(path: String, callback: TerminalMiddleware)
    fun options(path: String, callback: SimpleMiddleware)
    fun options(path: String, callback: Middleware)
    fun options(path: String, callback: Router)
    fun patch(path: String, callback: TerminalMiddleware)
    fun patch(path: String, callback: SimpleMiddleware)
    fun patch(path: String, callback: Middleware)
    fun patch(path: String, callback: Router)
    fun post(path: String, callback: TerminalMiddleware)
    fun post(path: String, callback: SimpleMiddleware)
    fun post(path: String, callback: Middleware)
    fun post(path: String, callback: Router)
    fun purge(path: String, callback: TerminalMiddleware)
    fun purge(path: String, callback: SimpleMiddleware)
    fun purge(path: String, callback: Middleware)
    fun purge(path: String, callback: Router)
    fun put(path: String, callback: TerminalMiddleware)
    fun put(path: String, callback: SimpleMiddleware)
    fun put(path: String, callback: Middleware)
    fun put(path: String, callback: Router)
    fun report(path: String, callback: TerminalMiddleware)
    fun report(path: String, callback: SimpleMiddleware)
    fun report(path: String, callback: Middleware)
    fun report(path: String, callback: Router)
    fun search(path: String, callback: TerminalMiddleware)
    fun search(path: String, callback: SimpleMiddleware)
    fun search(path: String, callback: Middleware)
    fun search(path: String, callback: Router)
    fun subscribe(path: String, callback: TerminalMiddleware)
    fun subscribe(path: String, callback: SimpleMiddleware)
    fun subscribe(path: String, callback: Middleware)
    fun subscribe(path: String, callback: Router)
    fun trace(path: String, callback: TerminalMiddleware)
    fun trace(path: String, callback: SimpleMiddleware)
    fun trace(path: String, callback: Middleware)
    fun trace(path: String, callback: Router)
    fun unlock(path: String, callback: TerminalMiddleware)
    fun unlock(path: String, callback: SimpleMiddleware)
    fun unlock(path: String, callback: Middleware)
    fun unlock(path: String, callback: Router)
    fun unsubscribe(path: String, callback: TerminalMiddleware)
    fun unsubscribe(path: String, callback: SimpleMiddleware)
    fun unsubscribe(path: String, callback: Middleware)
    fun unsubscribe(path: String, callback: Router)

    fun param(name: String, callback: ParamCallback)
    fun param(name: String, callback: NamedParamCallback)
    fun route(path: String): Router
    fun use(function: Router)
    fun use(function: Middleware)
    fun use(path: String, function: Middleware)
    fun use(path: String, function: Router)
}

private fun handlePromiseError(err: Throwable, res: Response) {
    val (errorCode: HttpStatusCode, errorMessage: String?, errorMime: MIMEType?) = when (err) {
        is RequestError -> Triple(err.code, err.message, err.mimeType)
        else -> Triple(HttpStatusCode.InternalServerError, null, null)
    }
    if (!res.headersSent) {
        if (errorMime != null) {
            res.setHeader(ContentTypeHeader.name, errorMime)
        }
        if (errorMessage != null) {
            res.status(errorCode)
            res.send(errorMessage)
        } else {
            res.sendStatus(errorCode)
        }
    } else if (!res.writableEnded) {
        res.status(errorCode)
        if (errorMessage != null)
            res.send("\n\n\n" + errorMessage)
        else
            res.end()
    }
}

private fun TerminalPromiseCreator.toMiddleware(): Middleware {
    return { req, res, next ->
        var prom: Promise<*>? = null
        var err: RequestError? = null
        try {
            prom = this(req, res)
        } catch (ex: RequestError) {
            err = ex
        }
        if (err == null)
            prom!!.catch {
                when (it) {
                    is RequestError -> handlePromiseError(it, res)
                    is NodeError -> next(it)
                    else -> {
                        handlePromiseError(it, res)
                        throw it
                    }
                }
            }
        else
            handlePromiseError(err, res)
    }
}

fun Router.allPromise(path: String, promiseCreator: TerminalPromiseCreator) = all(path, promiseCreator.toMiddleware())
fun Router.checkoutPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    checkout(path, promiseCreator.toMiddleware())

fun Router.copyPromise(path: String, promiseCreator: TerminalPromiseCreator) = copy(path, promiseCreator.toMiddleware())
fun Router.deletePromise(path: String, promiseCreator: TerminalPromiseCreator) =
    delete(path, promiseCreator.toMiddleware())

fun Router.getPromise(path: String, promiseCreator: TerminalPromiseCreator) = get(path, promiseCreator.toMiddleware())
fun Router.headPromise(path: String, promiseCreator: TerminalPromiseCreator) = head(path, promiseCreator.toMiddleware())
fun Router.lockPromise(path: String, promiseCreator: TerminalPromiseCreator) = lock(path, promiseCreator.toMiddleware())
fun Router.mergePromise(path: String, promiseCreator: TerminalPromiseCreator) =
    merge(path, promiseCreator.toMiddleware())

fun Router.mkactivityPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    mkactivity(path, promiseCreator.toMiddleware())

fun Router.mkcolPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    mkcol(path, promiseCreator.toMiddleware())

fun Router.movePromise(path: String, promiseCreator: TerminalPromiseCreator) = move(path, promiseCreator.toMiddleware())
fun Router.notifyPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    notify(path, promiseCreator.toMiddleware())

fun Router.optionsPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    options(path, promiseCreator.toMiddleware())

fun Router.patchPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    patch(path, promiseCreator.toMiddleware())

fun Router.postPromise(path: String, promiseCreator: TerminalPromiseCreator) = post(path, promiseCreator.toMiddleware())
fun Router.purgePromise(path: String, promiseCreator: TerminalPromiseCreator) =
    purge(path, promiseCreator.toMiddleware())

fun Router.putPromise(path: String, promiseCreator: TerminalPromiseCreator) = put(path, promiseCreator.toMiddleware())
fun Router.reportPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    report(path, promiseCreator.toMiddleware())

fun Router.searchPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    search(path, promiseCreator.toMiddleware())

fun Router.subscribePromise(path: String, promiseCreator: TerminalPromiseCreator) =
    subscribe(path, promiseCreator.toMiddleware())

fun Router.tracePromise(path: String, promiseCreator: TerminalPromiseCreator) =
    trace(path, promiseCreator.toMiddleware())

fun Router.unlockPromise(path: String, promiseCreator: TerminalPromiseCreator) =
    unlock(path, promiseCreator.toMiddleware())

fun Router.unsubscribePromise(path: String, promiseCreator: TerminalPromiseCreator) =
    unsubscribe(path, promiseCreator.toMiddleware())

abstract external class ExpressApp : Router {
    val locals: dynamic
    val mountpath: Any
    fun disable(name: String): Unit
    fun disabled(name: String): Boolean
    fun enable(name: String): Unit
    fun enabled(name: String): Boolean
    fun path(): String
    operator fun get(name: String): Any?
    operator fun set(name: String, value: Any?)
}