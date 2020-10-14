package de.robolab.client.net

import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IRESTResponse
import de.robolab.client.net.requests.RESTResult
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.headers.Header
import kotlin.jvm.JvmName

class RequestBuilder {
    private var method: HttpMethod = HttpMethod.GET
    private var protocol: String = "http"
        set(value) {
            field = value

            when (value.toLowerCase()) {
                "http" -> port = 80
                "https" -> port = 443
            }
        }
    private var host: String = ""
    private var port: Int = 80
    private var path: String = "/"
    private var body: String? = null
    private var query: MutableMap<String, String> = mutableMapOf()
    private var headers: MutableMap<String, List<String>> = mutableMapOf()

    fun method(method: HttpMethod) {
        this.method = method
    }

    fun get() = method(HttpMethod.GET)
    fun post() = method(HttpMethod.POST)
    fun put() = method(HttpMethod.PUT)
    fun delete() = method(HttpMethod.DELETE)

    fun protocol(protocol: String) {
        this.protocol = protocol
    }

    fun secure() = protocol("https")
    fun insecure() = protocol("http")

    fun host(host: String) {
        this.host = host
    }

    fun port(port: Int) {
        this.port = port
    }

    fun path(path: String) {
        this.path = path
    }

    fun url(url: String) {
        val result = urlPattern.matchEntire(url) ?: return

        val (protocol, host, port, path, query) = result.destructured

        if (protocol.isNotBlank()) {
            this.protocol = protocol.trim()
        }

        if (host.isNotBlank()) {
            this.host = host.trim()
        }

        if (port.isNotBlank()) {
            this.port = port.trim().toIntOrNull() ?: this.port
        }

        if (path.isNotBlank()) {
            this.path = path.trim().replace("//+".toRegex(), "/")
        }

        if (query.isNotBlank()) {
            for (pair in query.split('&')) {
                val split = pair.split('=')
                if (split.first().isNotBlank()) {
                    this.query.put(split.first(), split.getOrNull(1) ?: "")
                }
            }
        }
    }

    fun import(server: IRobolabServer) {
        protocol = server.protocol
        port = server.hostPort
        url(server.hostURL)
    }

    fun appendPath(append: String) {
        path += '/' + append.trim()
        path = path.replace("//+".toRegex(), "/")
    }

    fun body(body: String?) {
        this.body = body
    }

    fun query(query: Map<String, Any?>) {
        for ((key, value) in query) {
            this.query[key] = value?.toString() ?: ""
        }
    }

    fun query(vararg query: Pair<String, Any?>) {
        query(query.toMap())
    }

    fun header(header: Map<String, List<String>>) {
        this.headers.putAll(header)
    }

    fun header(vararg header: Pair<String, List<String>>) {
        val mapped = header.toMap()
        this.headers.putAll(mapped)
    }

    fun header(vararg header: Header){
        this.header(header.associate { it.name to it.value })
    }

    @JvmName("simpleHeader")
    fun header(header: Map<String, String>) {
        val mapped = header.mapValues { (_, v) -> listOf(v) }
        header(mapped)
    }

    @JvmName("simpleHeader")
    fun header(vararg header: Pair<String, String>) {
        val mapped = header.map { (k, v) -> k to listOf(v) }.toMap()
        header(mapped)
    }

    suspend fun exec(): ServerResponse {
        return sendHttpRequest(
            method,
            protocol,
            host,
            port,
            path,
            body,
            query,
            headers
        )
    }

    fun exec(callback: (ServerResponse) -> Unit) {
        sendHttpRequest(
            method,
            protocol,
            host,
            port,
            path,
            body,
            query,
            headers,
            callback
        )
    }

    fun <R> buildRequest(
        parser: (ServerResponse) -> RESTResult<R>
    ): IRESTRequest<R> where R : IRESTResponse {
        return object : IRESTRequest<R> {
            override val requestMethod: HttpMethod = this@RequestBuilder.method
            override val requestPath: String = this@RequestBuilder.path
            override val requestBody: String? = this@RequestBuilder.body
            override val requestQuery: Map<String, String> = this@RequestBuilder.query
            override val requestHeader: Map<String, List<String>> = this@RequestBuilder.headers

            override fun parseResponse(serverResponse: ServerResponse): RESTResult<R> = parser(serverResponse)
        }
    }

    companion object {
        private val urlPattern = """([a-zA-Z]*)(?:://)([^/:]*):?([0-9]*)([^?]*)\??(.*)""".toRegex()
    }
}

fun http(init: RequestBuilder.() -> Unit): RequestBuilder {
    val builder = RequestBuilder()
    builder.init()
    return builder
}

fun https(init: RequestBuilder.() -> Unit): RequestBuilder {
    val builder = RequestBuilder()
    builder.secure()
    builder.init()
    return builder
}
