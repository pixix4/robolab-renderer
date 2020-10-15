@file:Suppress("unused")

package de.robolab.server.externaljs.express

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.net.headers.ContentTypeHeader
import de.robolab.server.net.RESTResponseException
import de.robolab.server.auth.User
import de.robolab.server.externaljs.Buffer
import de.robolab.server.externaljs.JSArray
import de.robolab.server.externaljs.NodeError
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.jsutils.JSDynErrorCallback
import de.robolab.server.jsutils.PromiseScope
import de.robolab.server.jsutils.jsCreateDelegate
import de.robolab.server.jsutils.promise
import de.robolab.server.net.RESTResponseCodeException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.js.Promise
import kotlin.properties.ReadOnlyProperty

val Express = js("require(\"express\")")

fun createApp(): ExpressApp = Express()
    .unsafeCast<ExpressApp>()

const val caseSensitiveDefault: Boolean = false
const val mergeParamsDefault: Boolean = true
const val strictDefault: Boolean = false

typealias DefaultRequestData = Nothing?
typealias DefaultResponseData = Nothing?
typealias DefaultRequest = Request<DefaultRequestData>
typealias DefaultResponse = Response<DefaultResponseData>
private typealias AnyRequest = Request<*>
private typealias AnyResponse = Response<*>
private typealias AnyRouter = Router<*, *>

fun createRouter(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault
): DefaultRouter =
    createRouter<DefaultRequestData, DefaultResponseData>(caseSensitive, mergeParams, strict)

fun <ReqData, ResData> createRouter(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault
): Router<ReqData, ResData> =
    Express.Router(
        dynamicOf(
            "caseSensitive" to caseSensitive,
            "mergeParams" to mergeParams,
            "strict" to strict
        )
    ).unsafeCast<Router<ReqData, ResData>>()

interface Request<Data> : de.robolab.server.externaljs.http.IncomingServerMessage {
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
    var localData: Data
    var user: User

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

fun <ReqData, ReqDataT> Request<ReqData>.withData(data: ReqDataT): Request<ReqDataT> {
    return this.jsCreateDelegate("localData").unsafeCast<Request<ReqDataT>>().apply { this.localData = data }
}

val AnyRequest.paramProp: ReadOnlyProperty<Nothing?, String>
    get() = ReadOnlyProperty { _, property ->
        val value = this@paramProp.params[property.name]
        if (value == null || value == undefined) {
            throw IllegalArgumentException("Param ${property.name} not found in ${this@paramProp.params}!")
        }
        value as String
    }

val AnyRequest.queryProp: ReadOnlyProperty<Nothing?, String?>
    get() = ReadOnlyProperty { _, property -> this@queryProp.params[property.name] as String? }

fun AnyRequest.queryProp(default: String): ReadOnlyProperty<Nothing?, String> =
    ReadOnlyProperty { _, property -> this@queryProp.params[property.name] as String? ?: default }

val isMimeTypeProto = Express.request["is"]

fun AnyRequest.isMimeType(type: String): Any? {
    return isMimeTypeProto(this, type) as Any
}

fun AnyRequest.isMimeType(type: MIMEType): Any? {
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

val AnyRequest.mimeType: MIMEType?
    get() = contentTypeHeader?.mimeType

val AnyRequest.contentTypeHeader: ContentTypeHeader?
    get() {
        val headerValue: String? = this.headers[ContentTypeHeader.name] as? String
        return if (headerValue == null) null else ContentTypeHeader(headerValue)
    }

external interface Response<Data> : de.robolab.server.externaljs.http.ServerResponse {
    val app: ExpressApp
    val locals: dynamic
    var localData: Data

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

    fun status(code: Int): Response<Data>

    fun type(type: String): Response<Data>

    fun vary(field: String): Response<Data>
}

fun <ResData, ResDataT> Response<ResData>.withData(data: ResDataT): Response<ResDataT> {
    return this.jsCreateDelegate("localData").unsafeCast<Response<ResDataT>>().apply { this.localData = data }
}

fun <D> Response<D>.type(type: MIMEType): Response<D> = this.type(type.primaryName)

fun <D> Response<D>.formatReceiving(
    vararg handlers: Pair<String, Response<D>.() -> Unit>,
    defaultHandler: Response<D>.() -> Unit
) =
    format(*handlers.map { it.first to { it.second(this) } }.toTypedArray(), defaultHandler = { defaultHandler(this) })

fun <D> Response<D>.format(vararg handlers: Pair<String, () -> Unit>, defaultHandler: () -> Unit) =
    formatUnreceiving<D>(*handlers, defaultHandler = defaultHandler)(this)

fun <D> Response<D>.format(vararg handlers: Pair<String, () -> Unit>) =
    this.format(*handlers, defaultHandler = { sendStatus(HttpStatusCode.NotAcceptable) })

fun <D> Response<D>.formatReceiving(vararg handlers: Pair<String, Response<D>.() -> Unit>) =
    formatReceiving(*handlers, defaultHandler = { sendStatus(HttpStatusCode.NotAcceptable) })

fun <D> Response<D>.formatReceiving(vararg handlers: Pair<MIMEType, Response<D>.() -> Unit>) =
    formatReceiving(
        *(handlers.map { it.first.primaryName to it.second }).toTypedArray(),
        defaultHandler = { sendStatus(HttpStatusCode.NotAcceptable) })

fun <D> formatReceiving(vararg handlers: Pair<String, Response<D>.() -> Unit>): (Response<D>) -> Unit =
    formatReceiving(*handlers, defaultHandler = { sendStatus(HttpStatusCode.NotAcceptable) })

fun <D> formatReceiving(vararg handlers: Pair<MIMEType, Response<D>.() -> Unit>): (Response<D>) -> Unit =
    formatReceiving(*(handlers.map { it.first.primaryName to it.second }).toTypedArray())

fun <D> formatReceiving(
    vararg handlers: Pair<String, Response<D>.() -> Unit>,
    defaultHandler: Response<D>.() -> Unit
): (Response<D>) -> Unit {
    val obj: dynamic = js("{}")
    obj["default"] = defaultHandler
    handlers.forEach { obj[it.first] = it.second }
    return { it.format(obj) }
}

private fun <D> formatUnreceiving(
    vararg handlers: Pair<String, () -> Unit>,
    defaultHandler: () -> Unit
): (Response<D>) -> Unit {
    val obj: dynamic = js("{}")
    obj["default"] = defaultHandler
    handlers.forEach { obj[it.first] = it.second }
    return { it.format(obj) }
}

fun <D> Response<D>.status(status: HttpStatusCode): Response<D> = this.status(status.code)

fun Response<*>.sendStatus(status: HttpStatusCode) {
    //println("Instructed to send status $status")
    //console.log(this)
    this.sendStatus(status.code)
}
typealias TerminalMiddleware<ReqData, ResData> = (req: Request<ReqData>, res: Response<ResData>) -> Unit
typealias TerminalSuspender<ReqData, ResData> = suspend (req: Request<ReqData>, res: Response<ResData>) -> Unit
typealias TerminalPromiseCreator<ReqData, ResData> = (req: Request<ReqData>, res: Response<ResData>) -> Promise<*>
typealias SimpleMiddleware<ReqData, ResData> = (req: Request<ReqData>, res: Response<ResData>, next: () -> Unit) -> Unit
typealias Middleware<ReqData, ResData> = (req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit) -> Unit
typealias MiddlewareSuspender<ReqData, ResData> = suspend (req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit) -> Unit
typealias MiddlewarePromiseCreator<ReqData, ResData> = (req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit) -> Promise<*>
typealias ParamCallback<ReqData, ResData> = (req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit, value: String) -> Unit
typealias NamedParamCallback<ReqData, ResData> = (req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit, value: String, name: String) -> Unit

typealias DefaultTerminalMiddleware = TerminalMiddleware<DefaultRequestData, DefaultResponseData>
typealias DefaultTerminalSuspender = TerminalSuspender<DefaultRequestData, DefaultResponseData>
typealias DefaultTerminalPromiseCreator = TerminalPromiseCreator<DefaultRequestData, DefaultResponseData>
typealias DefaultSimpleMiddleware = SimpleMiddleware<DefaultRequestData, DefaultResponseData>
typealias DefaultMiddleware = Middleware<DefaultRequestData, DefaultResponseData>
typealias DefaultMiddlewareSuspender = MiddlewareSuspender<DefaultRequestData, DefaultResponseData>
typealias DefaultMiddlewarePromiseCreator = MiddlewarePromiseCreator<DefaultRequestData, DefaultResponseData>
typealias DefaultParamCallback = ParamCallback<DefaultRequestData, DefaultResponseData>
typealias DefaultNamedParamCallback = NamedParamCallback<DefaultRequestData, DefaultResponseData>

typealias DefaultRouter = Router<DefaultRequestData, DefaultResponseData>

external interface Router<ReqData, ResData> {
    fun all(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun all(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun all(path: String, callback: Middleware<ReqData, ResData>)
    fun all(path: String, callback: Router<in ReqData, in ResData>)
    fun checkout(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun checkout(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun checkout(path: String, callback: Middleware<ReqData, ResData>)
    fun checkout(path: String, callback: Router<in ReqData, in ResData>)
    fun copy(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun copy(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun copy(path: String, callback: Middleware<ReqData, ResData>)
    fun copy(path: String, callback: Router<in ReqData, in ResData>)
    fun delete(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun delete(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun delete(path: String, callback: Middleware<ReqData, ResData>)
    fun delete(path: String, callback: Router<in ReqData, in ResData>)
    fun get(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun get(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun get(path: String, callback: Middleware<ReqData, ResData>)
    fun get(path: String, callback: Router<in ReqData, in ResData>)
    fun head(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun head(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun head(path: String, callback: Middleware<ReqData, ResData>)
    fun head(path: String, callback: Router<in ReqData, in ResData>)
    fun lock(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun lock(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun lock(path: String, callback: Middleware<ReqData, ResData>)
    fun lock(path: String, callback: Router<in ReqData, in ResData>)
    fun merge(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun merge(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun merge(path: String, callback: Middleware<ReqData, ResData>)
    fun merge(path: String, callback: Router<in ReqData, in ResData>)
    fun mkactivity(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun mkactivity(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun mkactivity(path: String, callback: Middleware<ReqData, ResData>)
    fun mkactivity(path: String, callback: Router<in ReqData, in ResData>)
    fun mkcol(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun mkcol(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun mkcol(path: String, callback: Middleware<ReqData, ResData>)
    fun mkcol(path: String, callback: Router<in ReqData, in ResData>)
    fun move(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun move(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun move(path: String, callback: Middleware<ReqData, ResData>)
    fun move(path: String, callback: Router<in ReqData, in ResData>)
    fun notify(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun notify(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun notify(path: String, callback: Middleware<ReqData, ResData>)
    fun notify(path: String, callback: Router<in ReqData, in ResData>)
    fun options(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun options(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun options(path: String, callback: Middleware<ReqData, ResData>)
    fun options(path: String, callback: Router<in ReqData, in ResData>)
    fun patch(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun patch(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun patch(path: String, callback: Middleware<ReqData, ResData>)
    fun patch(path: String, callback: Router<in ReqData, in ResData>)
    fun post(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun post(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun post(path: String, callback: Middleware<ReqData, ResData>)
    fun post(path: String, callback: Router<in ReqData, in ResData>)
    fun purge(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun purge(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun purge(path: String, callback: Middleware<ReqData, ResData>)
    fun purge(path: String, callback: Router<in ReqData, in ResData>)
    fun put(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun put(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun put(path: String, callback: Middleware<ReqData, ResData>)
    fun put(path: String, callback: Router<in ReqData, in ResData>)
    fun report(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun report(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun report(path: String, callback: Middleware<ReqData, ResData>)
    fun report(path: String, callback: Router<in ReqData, in ResData>)
    fun search(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun search(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun search(path: String, callback: Middleware<ReqData, ResData>)
    fun search(path: String, callback: Router<in ReqData, in ResData>)
    fun subscribe(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun subscribe(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun subscribe(path: String, callback: Middleware<ReqData, ResData>)
    fun subscribe(path: String, callback: Router<in ReqData, in ResData>)
    fun trace(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun trace(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun trace(path: String, callback: Middleware<ReqData, ResData>)
    fun trace(path: String, callback: Router<in ReqData, in ResData>)
    fun unlock(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun unlock(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun unlock(path: String, callback: Middleware<ReqData, ResData>)
    fun unlock(path: String, callback: Router<in ReqData, in ResData>)
    fun unsubscribe(path: String, callback: TerminalMiddleware<ReqData, ResData>)
    fun unsubscribe(path: String, callback: SimpleMiddleware<ReqData, ResData>)
    fun unsubscribe(path: String, callback: Middleware<ReqData, ResData>)
    fun unsubscribe(path: String, callback: Router<in ReqData, in ResData>)

    fun param(name: String, callback: ParamCallback<ReqData, ResData>)
    fun param(name: String, callback: NamedParamCallback<ReqData, ResData>)
    fun route(path: String): Router<ReqData, ResData>
    fun use(function: Router<in ReqData, in ResData>)
    fun use(function: Middleware<ReqData, ResData>)
    fun use(path: String, function: Middleware<ReqData, ResData>)
    fun use(path: String, function: Router<ReqData, ResData>)
}

operator fun Router<*, *>.invoke(
    req: AnyRequest,
    res: AnyResponse,
    next: (NodeError?) -> Unit
) {
    //println("Invoking $this with $req, $res and $next")
    routerInvoker(this, req, res, next)
}

private val routerInvoker: (Router<*, *>, AnyRequest, AnyResponse, (NodeError?) -> Unit) -> Unit = js(
    "function routerInvokerJS(rtr,req,res,nxt){" +
            "return rtr(req,res,nxt);" +
            "}"
) as (Router<*, *>, AnyRequest, AnyResponse, (NodeError?) -> Unit) -> Unit


fun <ReqData, ResData> Router<ReqData, ResData>.all(callback: TerminalMiddleware<ReqData, ResData>) = all("/", callback)
fun <ReqData, ResData> Router<ReqData, ResData>.all(callback: SimpleMiddleware<ReqData, ResData>) = all("/", callback)
fun <ReqData, ResData> Router<ReqData, ResData>.all(callback: Middleware<ReqData, ResData>) = all("/", callback)
fun <ReqData, ResData> Router<ReqData, ResData>.all(callback: Router<in ReqData, in ResData>) = all("/", callback)


fun <ReqData, ResData, ReqDataT> Router<ReqData, ResData>.allMapRequest(
    transform: (Request<ReqData>) -> ReqDataT
): Router<ReqDataT, ResData> {
    return allMap(
        transformRequest = transform,
        transformResponse = { it.localData }
    )
}

fun <ReqData, ResData, ReqDataT> Router<ReqData, ResData>.allMapRequest(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>) -> ReqDataT
): Router<ReqDataT, ResData> {
    return allMap(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = transform,
        transformResponse = { it.localData }
    )
}

fun <ReqData, ResData, ReqDataT> Router<ReqData, ResData>.allMapRequestPromise(
    transform: (Request<ReqData>) -> Promise<ReqDataT>
): Router<ReqDataT, ResData> {
    return allMapPromise(
        transformRequest = transform,
        transformResponse = { Promise.resolve(it.localData) })
}

fun <ReqData, ResData, ReqDataT> Router<ReqData, ResData>.allMapRequestPromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>) -> Promise<ReqDataT>
): Router<ReqDataT, ResData> {
    return allMapPromise(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = transform,
        transformResponse = { Promise.resolve(it.localData) })
}

fun <ReqData, ResData, ReqDataT> Router<ReqData, ResData>.allMapRequestSuspend(
    transform: suspend (Request<ReqData>) -> ReqDataT
): Router<ReqDataT, ResData> {
    return allMapSuspend(
        transformRequest = transform,
        transformResponse = { it.localData }
    )
}

fun <ReqData, ResData, ReqDataT> Router<ReqData, ResData>.allMapRequestSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: suspend (Request<ReqData>) -> ReqDataT
): Router<ReqDataT, ResData> {
    return allMapSuspend(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = transform,
        transformResponse = { it.localData }
    )
}

fun <ReqData, ResData, ReqDataT> Router<ReqDataT, ResData>.asRequestMapped(
    transform: (Request<ReqData>) -> ReqDataT
): Router<ReqData, ResData> {
    return asMapped(
        transformRequest = transform,
        transformResponse = { it.localData })
}

fun <ReqData, ResData, ReqDataT> Router<ReqDataT, ResData>.asRequestMapped(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>) -> ReqDataT
): Router<ReqData, ResData> {
    return asMapped(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = transform,
        transformResponse = { it.localData })
}

fun <ReqData, ResData, ReqDataT> Router<ReqDataT, ResData>.asRequestMappedPromise(
    transform: (Request<ReqData>) -> Promise<ReqDataT>
): Router<ReqData, ResData> {
    return asMappedPromise(
        transformRequest = transform,
        transformResponse = { Promise.resolve(it.localData) })
}

fun <ReqData, ResData, ReqDataT> Router<ReqDataT, ResData>.asRequestMappedPromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>) -> Promise<ReqDataT>
): Router<ReqData, ResData> {
    return asMappedPromise(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = transform,
        transformResponse = { Promise.resolve(it.localData) })
}

fun <ReqData, ResData, ReqDataT> Router<ReqDataT, ResData>.asRequestMappedSuspend(
    transform: suspend (Request<ReqData>) -> ReqDataT
): Router<ReqData, ResData> {
    return asMappedSuspend(
        transformRequest = transform,
        transformResponse = { it.localData })
}

fun <ReqData, ResData, ReqDataT> Router<ReqDataT, ResData>.asRequestMappedSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: suspend (Request<ReqData>) -> ReqDataT
): Router<ReqData, ResData> {
    return asMappedSuspend(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = transform,
        transformResponse = { it.localData })
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResData>.allMapResponse(
    transform: (Response<ResData>) -> ResDataT
): Router<ReqData, ResDataT> {
    return allMap(
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResData>.allMapResponse(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Response<ResData>) -> ResDataT
): Router<ReqData, ResDataT> {
    return allMap(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResData>.allMapResponsePromise(
    transform: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqData, ResDataT> {
    return allMapPromise(
        transformRequest = { Promise.resolve(it.localData) },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResData>.allMapResponsePromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqData, ResDataT> {
    return allMapPromise(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = { Promise.resolve(it.localData) },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResData>.allMapResponseSuspend(
    transform: suspend (Response<ResData>) -> ResDataT
): Router<ReqData, ResDataT> {
    return allMapSuspend(
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResData>.allMapResponseSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: suspend (Response<ResData>) -> ResDataT
): Router<ReqData, ResDataT> {
    return allMapSuspend(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResDataT>.asResponseMapped(
    transform: (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMapped(
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResDataT>.asResponseMapped(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMapped(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResDataT>.asResponseMappedPromise(
    transform: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqData, ResData> {
    return asMappedPromise(
        transformRequest = { Promise.resolve(it.localData) },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResDataT>.asResponseMappedPromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqData, ResData> {
    return asMappedPromise(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = { Promise.resolve(it.localData) },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResDataT>.asResponseMappedSuspend(
    transform: suspend (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMappedSuspend(
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ResDataT> Router<ReqData, ResDataT>.asResponseMappedSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: suspend (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMappedSuspend(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict,
        transformRequest = { it.localData },
        transformResponse = transform
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMap(
    transformRequest: (Request<ReqData>) -> ReqDataT,
    transformResponse: (Response<ResData>) -> ResDataT
): Router<ReqDataT, ResDataT> {
    return allMap(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transformRequest = transformRequest,
        transformResponse = transformResponse
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMap(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transformRequest: (Request<ReqData>) -> ReqDataT,
    transformResponse: (Response<ResData>) -> ResDataT
): Router<ReqDataT, ResDataT> {
    return allMap(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    ) { req, res -> Pair(transformRequest(req), transformResponse(res)) }
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapPromise(
    transformRequest: (Request<ReqData>) -> Promise<ReqDataT>,
    transformResponse: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqDataT, ResDataT> {
    return allMapPromise(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transformRequest = transformRequest,
        transformResponse = transformResponse
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapPromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transformRequest: (Request<ReqData>) -> Promise<ReqDataT>,
    transformResponse: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqDataT, ResDataT> {
    return allMapPromise(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    ) { req, res ->
        Promise.all(arrayOf(transformRequest(req), transformResponse(res)))
            .then { Pair(it[0].unsafeCast<ReqDataT>(), it[1].unsafeCast<ResDataT>()) }
    }
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapSuspend(
    transformRequest: suspend (Request<ReqData>) -> ReqDataT,
    transformResponse: suspend (Response<ResData>) -> ResDataT
): Router<ReqDataT, ResDataT> {
    return allMapSuspend(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transformRequest = transformRequest,
        transformResponse = transformResponse
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transformRequest: suspend (Request<ReqData>) -> ReqDataT,
    transformResponse: suspend (Response<ResData>) -> ResDataT
): Router<ReqDataT, ResDataT> {
    return allMapSuspend(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    ) { req, res -> Pair(transformRequest(req), transformResponse(res)) }
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMapped(
    transformRequest: (Request<ReqData>) -> ReqDataT,
    transformResponse: (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMapped(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transformRequest = transformRequest,
        transformResponse = transformResponse
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMapped(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transformRequest: (Request<ReqData>) -> ReqDataT,
    transformResponse: (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMapped(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    ) { req, res -> Pair(transformRequest(req), transformResponse(res)) }
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedPromise(
    transformRequest: (Request<ReqData>) -> Promise<ReqDataT>,
    transformResponse: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqData, ResData> {
    return asMappedPromise(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transformRequest = transformRequest,
        transformResponse = transformResponse
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedPromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transformRequest: (Request<ReqData>) -> Promise<ReqDataT>,
    transformResponse: (Response<ResData>) -> Promise<ResDataT>
): Router<ReqData, ResData> {
    return asMappedPromise(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    ) { req, res ->
        Promise.all(arrayOf(transformRequest(req), transformResponse(res)))
            .then { Pair(it[0].unsafeCast<ReqDataT>(), it[1].unsafeCast<ResDataT>()) }
    }
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedSuspend(
    transformRequest: suspend (Request<ReqData>) -> ReqDataT,
    transformResponse: suspend (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMappedSuspend(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transformRequest = transformRequest,
        transformResponse = transformResponse
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transformRequest: suspend (Request<ReqData>) -> ReqDataT,
    transformResponse: suspend (Response<ResData>) -> ResDataT
): Router<ReqData, ResData> {
    return asMappedSuspend(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    ) { req, res ->
        Pair(transformRequest(req), transformResponse(res))
    }
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMap(
    transform: (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqDataT, ResDataT> {
    return allMap(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transform = transform
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMap(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqDataT, ResDataT> {
    val result: Router<ReqDataT, ResDataT> = createRouter<ReqDataT, ResDataT>(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    )
    all { req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit ->
        val (reqT: ReqDataT, resT: ResDataT) = transform(req, res)
        result(req.withData(reqT), res.withData(resT), next)
    }
    return result
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapPromise(
    transform: (Request<ReqData>, Response<ResData>) -> Promise<Pair<ReqDataT, ResDataT>>
): Router<ReqDataT, ResDataT> {
    return allMapPromise(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transform = transform
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapPromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>, Response<ResData>) -> Promise<Pair<ReqDataT, ResDataT>>
): Router<ReqDataT, ResDataT> {
    val result: Router<ReqDataT, ResDataT> = createRouter<ReqDataT, ResDataT>(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    )
    allPromise { req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit ->
        transform(req, res).then {
            val (reqT: ReqDataT, resT: ResDataT) = it
            result(req.withData(reqT), res.withData(resT), next)
        }
    }
    return result
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapSuspend(
    transform: suspend (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqDataT, ResDataT> {
    return allMapSuspend(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transform = transform
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqData, ResData>.allMapSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: suspend (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqDataT, ResDataT> {
    val result: Router<ReqDataT, ResDataT> = createRouter<ReqDataT, ResDataT>(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    )
    allSuspend { req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit ->
        val (reqT: ReqDataT, resT: ResDataT) = transform(req, res)
        result(req.withData(reqT), res.withData(resT), next)
    }
    return result
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMapped(
    transform: (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqData, ResData> {
    return asMapped(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transform = transform
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMapped(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqData, ResData> {
    val result: Router<ReqData, ResData> = createRouter<ReqData, ResData>(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    )
    result.all { req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit ->
        val (reqT: ReqDataT, resT: ResDataT) = transform(req, res)
        this(req.withData(reqT), res.withData(resT), next)
    }
    return result
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedPromise(
    transform: (Request<ReqData>, Response<ResData>) -> Promise<Pair<ReqDataT, ResDataT>>
): Router<ReqData, ResData> {
    return asMappedPromise(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transform = transform
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedPromise(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: (Request<ReqData>, Response<ResData>) -> Promise<Pair<ReqDataT, ResDataT>>
): Router<ReqData, ResData> {
    val result: Router<ReqData, ResData> = createRouter<ReqData, ResData>(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    )
    result.allPromise { req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit ->
        transform(req, res).then {
            val (reqT: ReqDataT, resT: ResDataT) = it
            this(req.withData(reqT), res.withData(resT), next)
        }
    }
    return result
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedSuspend(
    transform: suspend (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqData, ResData> {
    return asMappedSuspend(
        caseSensitive = caseSensitiveDefault,
        mergeParams = mergeParamsDefault,
        strict = strictDefault,
        transform = transform
    )
}

fun <ReqData, ResData, ReqDataT, ResDataT> Router<ReqDataT, ResDataT>.asMappedSuspend(
    caseSensitive: Boolean = caseSensitiveDefault,
    mergeParams: Boolean = mergeParamsDefault,
    strict: Boolean = strictDefault,
    transform: suspend (Request<ReqData>, Response<ResData>) -> Pair<ReqDataT, ResDataT>
): Router<ReqData, ResData> {
    val result: Router<ReqData, ResData> = createRouter<ReqData, ResData>(
        caseSensitive = caseSensitive,
        mergeParams = mergeParams,
        strict = strict
    )
    result.allSuspend { req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit ->
        val (reqT: ReqDataT, resT: ResDataT) = transform(req, res)
        this(req.withData(reqT), res.withData(resT), next)
    }
    return result
}

private fun handlePromiseError(err: Throwable, res: AnyResponse) {
    val (errorCode: HttpStatusCode, errorMessage: String?, errorMime: MIMEType?) = when (err) {
        is RESTResponseCodeException -> Triple(err.code, err.message, err.mimeType)
        is RESTResponseException -> Triple(HttpStatusCode.BadRequest, err.message, err.mimeType)
        else -> Triple(HttpStatusCode.InternalServerError, err.message, null)
    }
    if (err !is RESTResponseException)
        console.error(err)
    if (!res.headersSent) {
        if (errorMime != null) {
            res.setHeader(ContentTypeHeader.name, errorMime)
        }
        if (errorMessage != null) {
            res.setHeader("robolab-error", errorMessage)
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

private fun handlePromiseError(err: Throwable?, res: AnyResponse, next: (NodeError?) -> Unit) {
    when (err) {
        null -> next(err)
        is RESTResponseException -> handlePromiseError(err, res)
        is NodeError -> next(err)
        else -> {
            handlePromiseError(err, res)
            throw err
        }
    }
}

private fun <ReqData, ResData> TerminalPromiseCreator<ReqData, ResData>.toMiddleware(): Middleware<ReqData, ResData> {
    return { req, res, next ->
        var prom: Promise<*>? = null
        var err: RESTResponseException? = null
        try {
            prom = this(req, res)
        } catch (ex: RESTResponseException) {
            err = ex
        }
        if (err != null && err != undefined)
            handlePromiseError(err, res)
        else {
            prom!!.catch {
                handlePromiseError(it, res, next)
            }
        }
    }
}

private fun <ReqData, ResData> MiddlewarePromiseCreator<ReqData, ResData>.toMiddleware(): Middleware<ReqData, ResData> {
    return { req, res, next ->
        var prom: Promise<*>? = null
        var err: Throwable? = null
        var next2Ran = false
        val next2RanMutex = Mutex(false)
        fun next2(err2: Throwable?) {
            PromiseScope.launch {
                next2RanMutex.withLock {
                    if (next2Ran) return@launch
                    next2Ran = true
                }
                err = err2
                handlePromiseError(err, res, next)
            }
        }
        try {
            prom = this(req, res, ::next2)
        } catch (ex: RESTResponseException) {
            err = ex
        }
        if (err != null && err != undefined)
            next2(err)
        else
            prom!!.catch(::next2)
    }
}

private fun <ReqData, ResData> TerminalSuspender<ReqData, ResData>.toMiddleware(): Middleware<ReqData, ResData> {
    return { req: Request<ReqData>, res: Response<ResData> ->
        promise { this@toMiddleware(req, res) }
    }.toMiddleware()
}

private fun <ReqData, ResData> MiddlewareSuspender<ReqData, ResData>.toMiddleware(): Middleware<ReqData, ResData> {
    return { req: Request<ReqData>, res: Response<ResData>, next: (NodeError?) -> Unit ->
        promise { this@toMiddleware(req, res, next) }
    }.toMiddleware()
}


//region Promise Handlers
fun <ReqData, ResData> Router<ReqData, ResData>.allPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = all(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.allPromise(
    promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = all(promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.allPromise(
    path: String, promiseCreator: MiddlewarePromiseCreator<ReqData, ResData>
) = all(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.allPromise(
    promiseCreator: MiddlewarePromiseCreator<ReqData, ResData>
) = all(promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.checkoutPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = checkout(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.copyPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = copy(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.deletePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = delete(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.getPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = get(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.headPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = head(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.lockPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = lock(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.mergePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = merge(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.mkactivityPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = mkactivity(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.mkcolPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = mkcol(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.movePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = move(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.notifyPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = notify(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.optionsPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = options(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.patchPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = patch(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.postPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = post(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.purgePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = purge(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.putPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = put(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.reportPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = report(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.searchPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = search(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.subscribePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = subscribe(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.tracePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = trace(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.unlockPromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = unlock(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.unsubscribePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = unsubscribe(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.usePromise(
    path: String, promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = use(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.usePromise(
    promiseCreator: TerminalPromiseCreator<ReqData, ResData>
) = use(promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.usePromise(
    path: String, promiseCreator: MiddlewarePromiseCreator<ReqData, ResData>
) = use(path, promiseCreator.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.usePromise(
    promiseCreator: MiddlewarePromiseCreator<ReqData, ResData>
) = use(promiseCreator.toMiddleware())
//endregion


//region Suspending Handlers
fun <ReqData, ResData> Router<ReqData, ResData>.allSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = all(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.allSuspend(
    suspender: TerminalSuspender<ReqData, ResData>
) = all(suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.allSuspend(
    path: String, suspender: MiddlewareSuspender<ReqData, ResData>
) = all(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.allSuspend(
    suspender: MiddlewareSuspender<ReqData, ResData>
) = all(suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.checkoutSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = checkout(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.copySuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = copy(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.deleteSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = delete(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.getSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = get(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.headSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = head(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.lockSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = lock(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.mergeSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = merge(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.mkactivitySuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = mkactivity(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.mkcolSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = mkcol(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.moveSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = move(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.notifySuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = notify(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.optionsSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = options(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.patchSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = patch(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.postSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = post(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.purgeSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = purge(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.putSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = put(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.reportSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = report(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.searchSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = search(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.subscribeSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = subscribe(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.traceSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = trace(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.unlockSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = unlock(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.unsubscribeSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = unsubscribe(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.useSuspend(
    path: String, suspender: TerminalSuspender<ReqData, ResData>
) = use(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.useSuspend(
    suspender: TerminalSuspender<ReqData, ResData>
) = use(suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.useSuspend(
    path: String, suspender: MiddlewareSuspender<ReqData, ResData>
) = use(path, suspender.toMiddleware())

fun <ReqData, ResData> Router<ReqData, ResData>.useSuspend(
    suspender: MiddlewareSuspender<ReqData, ResData>
) = use(suspender.toMiddleware())
//endregion


external interface ExpressApp : DefaultRouter {
    val locals: dynamic
    val mountpath: Any
    fun disable(name: String)
    fun disabled(name: String): Boolean
    fun enable(name: String)
    fun enabled(name: String): Boolean
    fun path(): String
    operator fun get(name: String): Any?
    operator fun set(name: String, value: Any?)
}
