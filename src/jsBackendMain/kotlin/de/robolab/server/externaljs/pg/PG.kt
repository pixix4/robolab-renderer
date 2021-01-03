package de.robolab.server.externaljs.pg

import de.robolab.common.externaljs.JSArray
import kotlinx.coroutines.await
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.js.Promise

private val clientCreator: (config: dynamic) -> Client = js(
    "function (){" +
            "var pg = require(\"pg\");" +
            "return function (config){" +
            "return new pg.Client(config)" +
            "}" +
            "}"
)() as (dynamic) -> Client


fun obtainClient(connectionString: String): Client = clientCreator(connectionString)


external interface Client {
    fun connect(): Promise<Unit>
    fun query(text: String, values: JSArray<dynamic>? = definedExternally): Promise<Result>
    fun end(): Promise<Unit>
}

external interface Result {
    val rows: JSArray<dynamic>
    val fields: JSArray<FieldInfo>
    val command: String
    val rowCount: Int
}

external interface FieldInfo {
    val name: String
    val dataTypeId: dynamic
}

suspend inline fun <T> Client.withConnection(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    connect().await()
    try {
        return block()
    } finally {
        end().await()
    }
}
